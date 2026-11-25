package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

/**
 * A static replacement effect (CR 614.1a) that adds tokens to a token creation event under its controller's control:
 * "those tokens plus an additional Treasure token" (Xorn), "plus a 1/1 white Soldier" for creature tokens (Queen
 * Allenal of Ruadach), "plus that many Squirrels" (Chatterfang), "instead create a Food token and a Treasure token"
 * (Bilbo) and "instead create one of each" (Academy Manufactor). Each instance applies once to an event (CR 614.5);
 * the creating player orders it with the event's other replacements (CR 616.1), so a doubler applied after it doubles
 * the added tokens too, and it applies to tokens another replacement put in the event before it.
 *
 * <p>The added tokens get the creating effect's riders (tapped, attacking, "gains haste", exiled at end of combat,
 * ...), never the replaced tokens' abilities; a noncreature token added to tokens created attacking enters tapped. The
 * standard blueprints below are shared constants, so equal additions from different permanents are the same tokens
 * and orders that end alike need no prompt.
 *
 * @param tokens the blueprints of the added tokens, at least one
 * @param when   the tokens of the event that make it apply and that {@link Count#PER_TOKEN} and
 *               {@link Count#ONE_OF_EACH} count
 * @param count  how many of each blueprint are added
 */
public record AddTokenCreationEffect(List<CreateTokenEffect> tokens, TokenKind when, Count count)
        implements CardEffect {

    /** Treasure: "{T}, Sacrifice this artifact: Add one mana of any color." */
    public static final CreateTokenEffect TREASURE = CreateTokenEffect.ofTreasureToken(1);
    /** Clue: "{2}, Sacrifice this token: Draw a card." */
    public static final CreateTokenEffect CLUE = CreateTokenEffect.ofClueToken(1).withInvestigate(false);
    /** Food: "{2}, {T}, Sacrifice this token: You gain 3 life." */
    public static final CreateTokenEffect FOOD = CreateTokenEffect.ofFoodToken(1);
    /** Map: "{1}, {T}, Sacrifice this token: Target creature you control explores. Activate only as a sorcery." */
    public static final CreateTokenEffect MAP = CreateTokenEffect.ofMapToken(1);
    /**
     * Mutagen: "{1}, {T}, Sacrifice this token: Put a +1/+1 counter on target creature. Activate only as a
     * sorcery."
     */
    public static final CreateTokenEffect MUTAGEN = CreateTokenEffect.ofArtifactToken(1, "Mutagen",
            List.of(CardSubtype.MUTAGEN),
            List.of(new ActivatedAbility(true, "{1}",
                    List.of(new SacrificeSelfCost(), new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                    "{1}, {T}, Sacrifice this token: Put a +1/+1 counter on target creature. "
                            + "Activate only as a sorcery.",
                    TargetFilters.creature(), null, null, ActivationTimingRestriction.SORCERY_SPEED)));
    /** A 1/1 colorless Thopter artifact creature token with flying. */
    public static final CreateTokenEffect THOPTER = new CreateTokenEffect("Thopter", 1, 1, null,
            List.of(CardSubtype.THOPTER), Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT));
    /** A 1/1 green Frog creature token. */
    public static final CreateTokenEffect FROG = new CreateTokenEffect("Frog", 1, 1, CardColor.GREEN,
            List.of(CardSubtype.FROG), Set.of(), Set.of());
    /** A 1/1 green Squirrel creature token. */
    public static final CreateTokenEffect SQUIRREL = new CreateTokenEffect("Squirrel", 1, 1, CardColor.GREEN,
            List.of(CardSubtype.SQUIRREL), Set.of(), Set.of());
    /** A 1/1 white Soldier creature token. */
    public static final CreateTokenEffect SOLDIER = new CreateTokenEffect("Soldier", 1, 1, CardColor.WHITE,
            List.of(CardSubtype.SOLDIER), Set.of(), Set.of());

    /** How many tokens of each blueprint the replacement adds. */
    public enum Count {
        /** One of each for the whole event: "those tokens plus an additional [token]". */
        ONE,
        /** One of each for every token of {@link #when()}: "those tokens plus that many [tokens]". */
        PER_TOKEN,
        /**
         * For every token of {@link #when()}, one of each blueprint that token isn't already (shares no subtype
         * with): "instead create a Food token and a Treasure token", "instead create one of each".
         */
        ONE_OF_EACH
    }

    /**
     * Which tokens of an event the replacement looks at.
     *
     * @param type     a card type the token must have, or {@code null} for any token
     * @param subtypes subtypes of which the token must have at least one, or empty for any token
     */
    public record TokenKind(CardType type, Set<CardSubtype> subtypes) {

        public TokenKind {
            subtypes = subtypes == null ? Set.of() : Set.copyOf(subtypes);
        }

        /** "one or more tokens". */
        public static TokenKind any() {
            return new TokenKind(null, Set.of());
        }

        /** "one or more creature tokens". */
        public static TokenKind creature() {
            return new TokenKind(CardType.CREATURE, Set.of());
        }

        /** "one or more artifact tokens". */
        public static TokenKind artifact() {
            return new TokenKind(CardType.ARTIFACT, Set.of());
        }

        /** "a Clue, Food, or Treasure token". */
        public static TokenKind withAnySubtype(CardSubtype... subtypes) {
            return new TokenKind(null, Set.of(subtypes));
        }

        /**
         * Whether tokens of {@code blueprint} (a copy's kind for copies) are of this kind; {@code creature} says
         * whether they enter as creatures.
         */
        public boolean matches(CreateTokenEffect blueprint, boolean creature) {
            if (type == CardType.CREATURE && !creature) {
                return false;
            }
            if (type != null && type != CardType.CREATURE && blueprint.primaryType() != type
                    && (blueprint.additionalTypes() == null || !blueprint.additionalTypes().contains(type))) {
                return false;
            }
            return subtypes.isEmpty() || (blueprint.subtypes() != null
                    && blueprint.subtypes().stream().anyMatch(subtypes::contains));
        }
    }

    public AddTokenCreationEffect {
        tokens = List.copyOf(tokens);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("A token addition adds at least one token");
        }
    }

    /** "If [when] would be created under your control, those tokens plus [token] are created instead." */
    public static AddTokenCreationEffect plus(CreateTokenEffect token, TokenKind when) {
        return new AddTokenCreationEffect(List.of(token), when, Count.ONE);
    }

    /** "If one or more tokens would be created under your control, those tokens plus that many [token]s ..." */
    public static AddTokenCreationEffect plusThatMany(CreateTokenEffect token) {
        return new AddTokenCreationEffect(List.of(token), TokenKind.any(), Count.PER_TOKEN);
    }

    /** "If you would create [when], instead create one of each of [tokens]." */
    public static AddTokenCreationEffect oneOfEach(TokenKind when, CreateTokenEffect... tokens) {
        return new AddTokenCreationEffect(List.of(tokens), when, Count.ONE_OF_EACH);
    }
}
