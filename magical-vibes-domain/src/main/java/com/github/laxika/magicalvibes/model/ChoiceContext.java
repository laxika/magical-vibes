package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.effect.EffectDuration;

public sealed interface ChoiceContext {

    record TextChangeFromWord(UUID targetId) implements ChoiceContext {}

    record TextChangeToWord(UUID targetId, String fromWord, boolean isColor) implements ChoiceContext {}

    record ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                           boolean flashbackOnly, boolean instantSorceryOnly, boolean spellOrAbilitySubtype) implements ChoiceContext {

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
    }

    record DrawReplacementChoice(UUID playerId, DrawReplacementKind kind) implements ChoiceContext {}

    record CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes) implements ChoiceContext {}

    record KeywordGrantChoice(UUID targetId, List<Keyword> options) implements ChoiceContext {}

    record ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes) implements ChoiceContext {}

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
     * Vexing Arcanix: the target player names a card, then reveals the top card of their library.
     * If it matches the named card it goes to their hand; otherwise it goes to their graveyard and
     * the source ({@code sourcePermanentId}) deals 2 damage to them.
     */
    record VexingArcanixNameChoice(UUID controllerId, UUID targetPlayerId, UUID sourcePermanentId) implements ChoiceContext {}

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
}
