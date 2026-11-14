package com.github.laxika.magicalvibes.model.trigger;

/** A limit on how often a triggered ability triggers, from the sentence that follows it on the card. */
public enum TriggerLimit {
    NONE,
    /** "This ability triggers only once each turn." */
    ONCE_EACH_TURN,
    /**
     * "Do this only once each turn" after an instruction: the ability triggers only if its source's
     * controller hasn't yet taken that action this turn (CR 603.2h), so declining a "you may" leaves
     * it free to trigger again that turn.
     */
    DO_ONLY_ONCE_EACH_TURN,
    /** "This ability triggers only once." */
    ONCE,
    /**
     * Once each turn for each creature the event names: Stensia's "whenever a creature deals damage to one or more
     * players for the first time each turn", keyed by the source and the creature, and Stalwart Successor's "if it's
     * the first time counters have been put on that creature this turn". The damage and counter collectors judge it
     * from the ability ({@code DamageTriggers}, {@code CounterTriggers}), with the creature the event names.
     */
    ONCE_EACH_TURN_FOR_EACH_CREATURE,
    /**
     * "For the first time each turn" in the trigger condition: the ability triggers only for the first event of its
     * kind that turn, counted whether or not the source was there for the earlier ones, so a source that arrives later
     * doesn't trigger that turn: Akim, the Soaring Wind's "whenever you create one or more tokens for the first time
     * each turn", Crawling Sensation's "whenever one or more land cards are put into your graveyard from anywhere for
     * the first time each turn", Desert Were-Worm's "whenever you attack with creatures with total power 12 or greater
     * for the first time each turn" (its total power the event condition), Glyph Keeper's "whenever this creature
     * becomes the target of a spell or ability for the first time each turn" (every targeting of the permanent counts),
     * Aurelia, the Warleader's "whenever Aurelia attacks for the first time each turn" (the creature's first attack of
     * the turn, whatever the ability's "if" then says). Only the token-creation, land-to-graveyard, "whenever you
     * attack", "whenever this creature attacks" and "becomes the target of a spell or ability" collectors know it; the
     * trigger pipeline leaves it to them.
     */
    FIRST_TIME_EACH_TURN
}
