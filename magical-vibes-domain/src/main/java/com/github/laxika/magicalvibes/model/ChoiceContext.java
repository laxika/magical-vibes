package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;

public sealed interface ChoiceContext {

    record TextChangeFromWord(UUID targetId) implements ChoiceContext {}

    record TextChangeToWord(UUID targetId, String fromWord, boolean isColor) implements ChoiceContext {}

    record ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                           boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype,
                           List<ManaColor> fixedColorOptions, boolean creatureSpellOnly) implements ChoiceContext {

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                               boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly, spellOrAbilitySubtype, null, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature) {
            this(playerId, fromCreature, 1, null, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount) {
            this(playerId, fromCreature, amount, null, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, false, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly, boolean instantSorceryOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, instantSorceryOnly, false);
        }

        /** "Add N mana of any one color, spendable only to cast instant/sorcery spells" (e.g. Resonating Lute). */
        public static ManaColorChoice instantSorceryOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, true, false);
        }

        /**
         * "Add N mana in any combination of colors, spendable only to cast spells of {@code subtype}
         * or activate abilities of permanents of that subtype" (e.g. Smokebraider). Each mana's color
         * is chosen individually (any combination).
         */
        public static ManaColorChoice subtypeSpellOrAbility(UUID playerId, int amount, CardSubtype subtype) {
            return new ManaColorChoice(playerId, false, amount, subtype, false, false, true);
        }

        /**
         * "Add N mana, each chosen individually from a fixed list of colors" (filter lands such as
         * Fire-Lit Thicket's "Add {R}{R}, {R}{G}, or {G}{G}"). Each mana's color is picked separately
         * from {@code colors}, re-prompting until all {@code amount} have been chosen.
         */
        public static ManaColorChoice fixedColorCombination(UUID playerId, boolean fromCreature, int amount, List<ManaColor> colors) {
            return new ManaColorChoice(playerId, fromCreature, amount, null, false, false, false, colors, false);
        }

        /**
         * "Add N mana of any one color, spendable only to cast a creature spell of any type"
         * (Ancient Ziggurat). The color is chosen at activation; the mana routes to the pool's
         * creature-spell-only bucket.
         */
        public static ManaColorChoice creatureSpellOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, false, false, null, true);
        }
    }

    record DrawReplacementChoice(UUID playerId, DrawReplacementKind kind) implements ChoiceContext {}

    record CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes) implements ChoiceContext {}

    record KeywordGrantChoice(UUID targetId, List<Keyword> options) implements ChoiceContext {}

    record ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes) implements ChoiceContext {}

    /**
     * The controller chose a card name; {@code targetPlayerId} reveals their hand, the source deals
     * {@code damagePerCard} damage per revealed copy, then every copy in their hand/graveyard/library
     * is exiled and they shuffle (Thought Hemorrhage). {@code sourceCard} attributes the damage.
     */
    record RevealHandDamageAndExileByNameChoice(UUID targetPlayerId, UUID controllerId,
                                                List<CardType> excludedTypes, int damagePerCard,
                                                Card sourceCard) implements ChoiceContext {}

    record ProtectionColorChoice(UUID targetId, boolean includeArtifacts) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution; the target permanent then becomes that color
     * until end of turn (CR 105.3 / layer 5). Used by Distorting Lens.
     *
     * @param targetId       the permanent that becomes the chosen color
     * @param controllerId   controller of the ability that created the effect
     * @param sourceCardName name of the card whose ability created the effect
     */
    record ColorSetChoice(UUID targetId, UUID controllerId, String sourceCardName) implements ChoiceContext {}

    /**
     * A single color choice that grants the chosen controller and each permanent they control
     * protection from the chosen color until end of turn (e.g. Faith's Shield fateful hour).
     */
    record MassProtectionColorChoice(UUID controllerId) implements ChoiceContext {}

    record SubtypeChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * The controller chooses a creature type at resolution for a spell/ability that has no
     * permanent to store it on (e.g. Coordinated Barrage). The answer is stored on
     * {@code GameData.chosenSpellSubtype} and effect resolution resumes.
     */
    record SpellCreatureTypeChoice(UUID controllerId) implements ChoiceContext {}

    /** Choosing odd or even "as this permanent enters" (Ashling's Prerogative). */
    record ManaValueParityChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * Choosing a number in an inclusive range for {@code permanentId} (e.g. Shapeshifter's "choose a
     * number between 0 and 7", both as it enters and at each upkeep). The answer is stored on the
     * permanent via {@code Permanent.setChosenNumber(int)}.
     */
    record NumberChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * Choosing how many {@code counterType} counters to remove from {@code permanentId} as a
     * storage land's mana ability resolves (0..the count present). On resume the chosen number of
     * counters is removed and that much mana of {@code color} is added to {@code playerId}'s pool
     * (times {@code manaMultiplier} for Mana Reflection; {@code fromCreature} marks creature mana).
     * Used by the storage-land cycle via {@code RemoveCountersForManaEffect}.
     */
    record RemoveCountersForManaChoice(UUID playerId, UUID permanentId, ManaColor color,
                                       CounterType counterType, boolean fromCreature,
                                       int manaMultiplier) implements ChoiceContext {}

    /**
     * Tetravus first upkeep trigger: the controller chooses how many of {@code permanentId}'s +1/+1
     * counters (0..the count present) to remove; on the answer that many are removed and that many
     * Tetravite tokens are created from {@code tokenTemplate}, each recorded as "created with" the
     * source in {@code GameData.tetravusCreatedTokens} (read by the paired exile trigger).
     */
    record TetravusCounterRemoval(UUID permanentId,
                                  com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate)
            implements ChoiceContext {}

    /** Choosing one of Primal Clay's three shapes "as this creature enters". */
    record PrimalClayFormChoice(UUID permanentId) implements ChoiceContext {}

    record BasicLandTypeChoice(UUID permanentId) implements ChoiceContext {}

    /**
     * Choosing a basic land type for a target land: either added "in addition to its other types"
     * (Navigator's Compass) or, when {@code replacing} is {@code true}, replacing the land's other
     * types and mana ability per rule 305.7 (Tideshaper Mystic).
     *
     * @param targetLandId the target land that gains/becomes the chosen basic land type
     * @param duration     how long the granted/overriding type lasts
     * @param replacing    {@code true} to replace the land's types, {@code false} to add
     */
    record AddBasicLandTypeChoice(UUID targetLandId, EffectDuration duration, boolean replacing) implements ChoiceContext {

        public AddBasicLandTypeChoice(UUID targetLandId, EffectDuration duration) {
            this(targetLandId, duration, false);
        }
    }

    /**
     * The controller has chosen a basic land type; each land they control becomes that type
     * until end of turn, replacing its other land types/mana ability per rule 305.7
     * (Elsewhere Flask).
     *
     * @param controllerId the player whose lands become the chosen type
     */
    record OwnLandsBecomeBasicTypeChoice(UUID controllerId) implements ChoiceContext {
    }

    /**
     * Tracks the sequential "each player names a card" flow for Conundrum Sphinx etc.
     * Players name in APNAP order. After all have named, top cards are revealed and
     * moved to hand (match) or bottom of library (no match).
     *
     * @param playerOrder  all player IDs in APNAP order
     * @param chosenNames  names chosen so far (playerId → chosen name)
     */
    record EachPlayerCardNameRevealChoice(List<UUID> playerOrder,
                                          Map<UUID, String> chosenNames) implements ChoiceContext {}

    /**
     * Sphinx Ambassador: the damaged player names a card after the controller has selected
     * a card from their library. The selected card is stored in
     * the queued {@code PendingSphinxAmbassadorChoice} interaction.
     */
    record SphinxAmbassadorNameChoice(UUID namingPlayerId, UUID controllerId) implements ChoiceContext {}

    /**
     * Lammastide Weave: the controller names a card, then the target player mills one card. If the
     * milled card matches the chosen name, the controller gains life equal to its mana value.
     */
    record NameCardMillGainLifeChoice(UUID controllerId, UUID targetPlayerId) implements ChoiceContext {}

    /**
     * The target player names a card, then reveals the top card of their library. If it matches
     * the named card it goes to their hand; otherwise it goes to their graveyard and the source
     * ({@code sourcePermanentId}) deals {@code damageOnMiss} damage to them ({@code 0} for no
     * damage). Used by Vexing Arcanix.
     */
    record TargetPlayerNameCardRevealTopChoice(UUID controllerId, UUID targetPlayerId, UUID sourcePermanentId,
                                               int damageOnMiss) implements ChoiceContext {}

    /**
     * The controller chooses a permanent type at resolution time (e.g. Creeping Renaissance),
     * then all cards of that type are returned from the controller's graveyard.
     */
    record PermanentTypeChoice(UUID controllerId, GraveyardChoiceDestination destination,
                               String entryDescription) implements ChoiceContext {}

    /**
     * Tracks a "choose a mana color, add N of it" choice for effects like Grand Warlord Radha.
     * Also sets mana drain prevention for the controller until end of turn.
     *
     * @param playerId       the player who chooses and receives the mana
     * @param attackerCount  the number of attacking creatures (amount of mana to add)
     */
    record AttackManaSplitChoice(UUID playerId, int attackerCount) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then {@code targetPlayerId} reveals their hand
     * and discards every card of that color (Persecute).
     */
    record DiscardChosenColorChoice(UUID controllerId, UUID targetPlayerId) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution; {@code targetPlayerId} then exiles the top
     * {@code count} cards of their library and, for each exiled card of the chosen color, the
     * controller creates a token from {@code tokenTemplate} (Oona, Queen of the Fae).
     *
     * @param controllerId   controller that chooses the color and creates the tokens
     * @param targetPlayerId the target opponent who exiles cards
     * @param count          number of top cards to exile
     * @param tokenTemplate  one token created per exiled card of the chosen color
     * @param sourceSetCode  set code of the source card (token art/set)
     */
    record ExileTopCardsChosenColorTokensChoice(UUID controllerId, UUID targetPlayerId, int count,
                                                com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                                String sourceSetCode) implements ChoiceContext {}

    /**
     * The controller chooses a color at resolution, then creates one token from {@code tokenTemplate}
     * for each permanent of that color on the battlefield (any controller; lands excluded, mirroring
     * Oona). Rith, the Awakener.
     *
     * @param controllerId  controller that chooses the color and creates the tokens
     * @param tokenTemplate one token created per permanent of the chosen color
     * @param sourceSetCode set code of the source card (token art/set)
     */
    record CreateTokensPerPermanentOfChosenColorChoice(UUID controllerId,
                                                       com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate,
                                                       String sourceSetCode) implements ChoiceContext {}

    /**
     * Storage Matrix: during {@code playerId}'s untap step the active player chooses artifact,
     * creature, or land; only permanents of the chosen type untap this step.
     */
    record StorageMatrixUntapChoice(UUID playerId) implements ChoiceContext {}

    /**
     * Prismwake Merrow: the controller chooses one or more colors for {@code targetId}, which then
     * becomes those colors until end of turn. Colors are picked one at a time (with a "DONE" option
     * once at least one is chosen); {@code chosen} accumulates the picks so far.
     *
     * @param targetId       the permanent that becomes the chosen colors
     * @param sourceCardName name of the card whose ability created the effect (for display)
     * @param chosen         the colors picked so far
     */
    record BecomeChosenColorsChoice(UUID targetId, String sourceCardName,
                                    List<CardColor> chosen) implements ChoiceContext {}

    /**
     * Relic Bind's "choose one" mode pick, made as the enchanted-artifact-tap triggered ability
     * resolves. {@code sourceCard} is the Aura (used for the follow-up target choice and logging);
     * {@code controllerId} chooses the mode and then the target. The two options are
     * {@link #DAMAGE} (deal 1 damage to target player or planeswalker) and {@link #LIFE}
     * (target player gains 1 life).
     */
    record RelicBindModeChoice(Card sourceCard, UUID controllerId) implements ChoiceContext {

        public static final String DAMAGE = "Deal 1 damage to target player or planeswalker";
        public static final String LIFE = "Target player gains 1 life";
        public static final List<String> OPTIONS = List.of(DAMAGE, LIFE);
    }

    /**
     * Quarry Hauler: "for each kind of counter on target permanent, put another counter of that kind
     * on it or remove one from it." The controller answers {@link #ADD}/{@link #REMOVE} once for the
     * first entry of {@code remainingKinds}; the answer is applied to {@code targetId} and, if any
     * kinds are left, the choice re-prompts for the next one until every kind has been resolved.
     *
     * @param targetId       the permanent whose counters are being adjusted
     * @param controllerId   the player making the add/remove decisions
     * @param sourceCardName name of the source card (for the prompt/log)
     * @param remainingKinds the counter kinds still awaiting a decision (first is the current one)
     */
    record AdjustCounterKindChoice(UUID targetId, UUID controllerId, String sourceCardName,
                                   List<CounterType> remainingKinds) implements ChoiceContext {

        public static final String ADD = "ADD";
        public static final String REMOVE = "REMOVE";
        public static final List<String> OPTIONS = List.of(ADD, REMOVE);
    }

    /**
     * A modal triggered ability's "choose one" mode pick, made as the ability resolves (the engine
     * has no cast-time modal machinery for triggered abilities). {@code sourceCard} is the ability's
     * source (used for logging), {@code controllerId} chooses the mode, and {@code effect} carries the
     * {@link ChooseOneEffect}'s options; the chosen mode's effects are spliced into the paused
     * resolution. Used by non-targeting modal upkeep triggers such as Etherwrought Page.
     */
    record ChooseModeChoice(Card sourceCard, UUID controllerId, ChooseOneEffect effect) implements ChoiceContext {}

    /**
     * Torment of Hailfire: {@code affectedPlayerId} (an opponent) chooses one of "sacrifice a nonland
     * permanent", "discard a card", or "lose N life" for one iteration of the effect. The offered
     * options are pruned to what the player can actually do (life is always offered); the "lose life"
     * label is dynamic ({@code "Lose N life"}), so a chosen value that is neither {@link #SACRIFICE}
     * nor {@link #DISCARD} means the life-loss outcome. Answered via {@code handleListChoice}.
     */
    record TormentPenaltyChoice(UUID affectedPlayerId, String sourceCardName) implements ChoiceContext {

        public static final String SACRIFICE = "Sacrifice a nonland permanent";
        public static final String DISCARD = "Discard a card";
    }
}
