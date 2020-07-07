package bot.modules;

import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
	/**
	 * An enum representing the different types of arguments passed to a command.
	 * Can also be checked for validity.
	 */
	public enum ArgType {
		INVALID(false),
		NHENTAI(true),
		EHENTAI(true),
		VALID(true);

		private final boolean valid;
		ArgType(final boolean valid) {
			this.valid = valid;
		}
		public boolean isValid() {
			return valid;
		}
	}

	/**
	 * A list of all commands the bot supports.
	 */
	private static final HashSet<String> allCommands = new HashSet<>(
			Arrays.asList(
					"help", "tags", "getpage", "info", "badtags", "warningtags",
					"random", "search", "deepsearch", "addhook", "removehook",
					"searcheh", "deepsearcheh", "setprefix"
			)
	);

	/**
	 * A regex representing the cursed numbers.
	 */
	private static final Pattern cursed = Pattern.compile("https?://nhentai\\.net/g/177013/?");

	/**
	 * Checks if a given query is a command.
	 * @param query
	 * @return
	 */
	public static boolean isCommand(String query) {
		return allCommands.contains(query);
	}

	/**
	 * Validates an argument using a passed array of regexes,
	 * @param args The arguments provided in the command.
	 * @param argCount The number of arguments that should be present.
	 * @param regexes The regexes that the arguments should match at least one of.
	 * @param channel The message channel to send validation failure messages to.
	 * @return An ArgType representing whether the argument was valid or not.
	 */
	public static ArgType validate(String args, int argCount, Pattern[] regexes, MessageChannel channel) {
		if(args.split(" ").length < argCount) {
			channel.sendMessage("Please supply a link or numbers!").queue();
			return ArgType.INVALID;
		}
		Matcher cursedMatcher = cursed.matcher(args);
		if (cursedMatcher.find()) {
			channel.sendMessage(EmbedGenerator.cursedEmbed()).queue();
			return ArgType.INVALID;
		}

		boolean matches = false;
		for(Pattern cur : regexes) {
			Matcher curMatcher = cur.matcher(args);
			if(curMatcher.find()) {
				matches = true;
			}
		}

		if(!matches) {
			channel.sendMessage("Not a supported site / valid URL!").queue();
			return ArgType.INVALID;
		}
		return ArgType.VALID;
	}

	/**
	 * Gets the corresponding ArgType of a URL.
	 * @param url The URL to get the ArgType of.
	 * @return The corresponding ArgType of the URL. If no matching ArgType is found, this returns ArgType.INVALID.
	 */
	public static ArgType getArgType(String url) {
		Pattern ehPage = Pattern.compile("https?://e[x\\-]hentai\\.org/s/([\\da-f]{10})/(\\d+)-(\\d+)/");
		Pattern ehGallery = Pattern.compile("https?://e[x\\-]hentai\\.org/g/(\\d+)/([\\da-f]{10})/?");
		Pattern nhGallery = Pattern.compile("https?://nhentai\\.net/g/\\d{1,6}/?");

		Matcher nhGalleryMatcher = nhGallery.matcher(url);
		if(nhGalleryMatcher.find()) {
			return ArgType.NHENTAI;
		}
		Matcher ehGalleryMatcher = ehGallery.matcher(url);
		if(ehGalleryMatcher.find()) {
			return ArgType.EHENTAI;
		}
		Matcher ehPageMatcher = ehPage.matcher(url);
		if(ehPageMatcher.find()) {
			return ArgType.EHENTAI;
		}

		return ArgType.INVALID;
	}

	/**
	 * {@code argCount} defaults to 1
	 *
	 * @see Validator#siteValidate(String, int, MessageChannel)
	 */
	public static ArgType siteValidate(String args, MessageChannel channel) {
		return siteValidate(args, 1, channel);
	}

	/**
	 * Validates an argument for a specific site.
	 * @param args The arguments provided in the command.
	 * @param argCount The number of arguments that should be present.
	 * @param channel The message channel to send validation failure messages to.
	 * @return An ArgType representing what the site of the argument is.
	 */
	public static ArgType siteValidate(String args, int argCount, MessageChannel channel) {
		Pattern ehPage = Pattern.compile("https?://e[x\\-]hentai\\.org/s/([\\da-f]{10})/(\\d+)-(\\d+)/");
		Pattern ehGallery = Pattern.compile("https?://e[x\\-]hentai\\.org/g/(\\d+)/([\\da-f]{10})/?");
		Pattern nhGallery = Pattern.compile("https?://nhentai\\.net/g/\\d{1,6}/?");
		if(!validate(args, argCount, new Pattern[]{ehPage, ehGallery, nhGallery}, channel).isValid()) {
			return ArgType.INVALID;
		}

		return getArgType(args);
	}
}