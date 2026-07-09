package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.effect.EffectDuration;

public sealed interface ChoiceContext {

    record TextChangeFromWord(UUID targetId) implements ChoiceContext {}

    record TextChangeToWord(UUID targetId, String fromWord, boolean isColor) implements ChoiceContext {}

    record ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype,
                           boolean flashbackOnly, boolean instantSorceryOnly) implements ChoiceContext {

        public ManaColorChoice(UUID playerId, boolean fromCreature) {
            this(playerId, fromCreature, 1, null, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount) {
            this(playerId, fromCreature, amount, null, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, false, false);
        }

        public ManaColorChoice(UUID playerId, boolean fromCreature, int amount, CardSubtype restrictedToCreatureSubtype, boolean flashbackOnly) {
            this(playerId, fromCreature, amount, restrictedToCreatureSubtype, flashbackOnly, false);
        }

        /** "Add N mana of any one color, spendable only to cast instant/sorcery spells" (e.g. Resonating Lute). */
        public static ManaColorChoice instantSorceryOnly(UUID playerId, int amount) {
            return new ManaColorChoice(playerId, false, amount, null, false, true);
        }
    }

    record DrawReplacementChoice(UUID playerId, DrawReplacementKind kind) implements ChoiceContext {}

    record CardNameChoice(Card card, UUID controllerId, List<CardType> excludedTypes) implements ChoiceContext {}

    record KeywordGrantChoice(UUID targetId, List<Keyword> options) implements ChoiceContext {}

    record ExileByNameChoice(UUID targetPlayerId, UUID controllerId, List<CardType> excludedTypes) implements ChoiceContext {}

    record ProtectionColorChoice(UUID targetId, boolean includeArtifacts) implements ChoiceContext {}

    /**
     * A single color choice that grants the chosen controller and each permanent they control
     * protection from the chosen color until end of turn (e.g. Faith's Shield fateful hour).
     */
    record MassProtectionColorChoice(UUID controllerId) implements ChoiceContext {}

    record SubtypeChoice(UUID permanentId) implements ChoiceContext {}

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
}
