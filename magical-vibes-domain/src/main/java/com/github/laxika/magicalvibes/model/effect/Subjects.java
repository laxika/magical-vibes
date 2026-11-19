package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

/**
 * The named factory behind every {@link PermanentSubject} and {@link PlayerSubject}, so an effect reads like its card
 * text: {@code controllerOf(triggering())} is "that creature's controller". The variants are listed in
 * {@code agent-docs/SUBJECTS_REFERENCE.md}.
 */
public final class Subjects {

    private static final PermanentSubject SOURCE = new PermanentSubject.Source();
    private static final PermanentSubject TARGET = new PermanentSubject.Target(null);
    private static final PermanentSubject ATTACHED = new PermanentSubject.Attached();
    private static final PermanentSubject ATTACHED_AT_TRIGGER = new PermanentSubject.AttachedAtTrigger();
    private static final PermanentSubject TRIGGERING = new PermanentSubject.Triggering();
    private static final PermanentSubject TRIGGERING_CARD = new PermanentSubject.TriggeringCard();
    private static final PermanentSubject TARGET_SPELL = new PermanentSubject.TargetSpell();
    private static final PermanentSubject TRIGGERING_SPELL = new PermanentSubject.TriggeringSpell();
    private static final PermanentSubject RETURNED = new PermanentSubject.Returned();

    private static final PlayerSubject YOU = new PlayerSubject.You();
    private static final PlayerSubject TARGET_PLAYER = new PlayerSubject.TargetPlayer(null, true);
    private static final PlayerSubject TARGET_PLAYER_DECLARED_ELSEWHERE = new PlayerSubject.TargetPlayer(null, false);
    private static final PlayerSubject EACH_PLAYER = new PlayerSubject.Each(PlayerSubject.Relation.ALL);
    private static final PlayerSubject EACH_OPPONENT = new PlayerSubject.Each(PlayerSubject.Relation.OPPONENTS);
    private static final PlayerSubject TRIGGERING_PLAYER = new PlayerSubject.TriggeringPlayer();
    private static final PlayerSubject ACTIVE_PLAYER = new PlayerSubject.ActivePlayer();
    private static final PlayerSubject DEFENDING_PLAYER = new PlayerSubject.DefendingPlayer();
    private static final PlayerSubject ENCHANTED_PLAYER = new PlayerSubject.EnchantedPlayer();
    private static final PlayerSubject CHOSEN_PLAYER = new PlayerSubject.ChosenPlayer();
    private static final PlayerSubject CHOSEN_PLAYERS = new PlayerSubject.ChosenPlayers();
    private static final PlayerSubject ACCEPTED_PLAYERS = new PlayerSubject.AcceptedPlayers();

    private Subjects() {
    }

    /** "This permanent". */
    public static PermanentSubject source() {
        return SOURCE;
    }

    /** "Target permanent": the targets the stack entry assigns to the effect. */
    public static PermanentSubject target() {
        return TARGET;
    }

    /** The targets chosen for target group {@code group}. */
    public static PermanentSubject target(int group) {
        return new PermanentSubject.Target(group);
    }

    /** "Enchanted creature", "equipped creature". */
    public static PermanentSubject attached() {
        return ATTACHED;
    }

    /**
     * "That creature" naming the host from the trigger condition: the permanent the source was attached to when the
     * ability triggered or was activated.
     */
    public static PermanentSubject attachedAtTrigger() {
        return ATTACHED_AT_TRIGGER;
    }

    /** "It", "that creature": the permanent the trigger event was about. */
    public static PermanentSubject triggering() {
        return TRIGGERING;
    }

    /**
     * "That card": the spell cast or land played the trigger was about; with {@link #ownerOf}, also the card a
     * graveyard or zone-change event was about, or the permanent that entered, as it is or last was.
     */
    public static PermanentSubject triggeringCard() {
        return TRIGGERING_CARD;
    }

    /** "Target spell", for {@link #controllerOf}: "that spell's controller". Declares nothing by itself. */
    public static PermanentSubject targetSpell() {
        return TARGET_SPELL;
    }

    /** "That spell or ability", for {@link #controllerOf}: the spell or ability the trigger event was about. */
    public static PermanentSubject triggeringSpell() {
        return TRIGGERING_SPELL;
    }

    /** The permanent a preceding "return target card from a graveyard to the battlefield" put onto the battlefield. */
    public static PermanentSubject returned() {
        return RETURNED;
    }

    /** Every permanent matching {@code filter} that {@code players} control. */
    public static PermanentSubject controlledBy(PlayerSubject players, PermanentPredicate filter) {
        return new PermanentSubject.ControlledBy(players, filter);
    }

    /** Every permanent {@code players} control. */
    public static PermanentSubject controlledBy(PlayerSubject players) {
        return new PermanentSubject.ControlledBy(players, new PermanentTruePredicate());
    }

    /** Several subjects together, each permanent once. */
    public static PermanentSubject allOf(PermanentSubject... subjects) {
        return new PermanentSubject.AllOf(List.of(subjects));
    }

    /**
     * The subject a {@link PermanentReference} names. A temporary adapter for the effects that still carry a
     * {@code PermanentReference} component; it goes when the last of them takes a {@link PermanentSubject}.
     */
    public static PermanentSubject of(PermanentReference reference) {
        return switch (reference) {
            case SOURCE -> SOURCE;
            case ATTACHED -> ATTACHED;
            case ATTACHED_AT_TRIGGER -> ATTACHED_AT_TRIGGER;
            case TRIGGERING -> TRIGGERING;
            case RETURNED -> RETURNED;
        };
    }

    /** "You". */
    public static PlayerSubject you() {
        return YOU;
    }

    /** "Target player": the player targets the stack entry assigns to the effect. */
    public static PlayerSubject targetPlayer() {
        return TARGET_PLAYER;
    }

    /** The player targets chosen for target group {@code group}. */
    public static PlayerSubject targetPlayer(int group) {
        return new PlayerSubject.TargetPlayer(group, true);
    }

    /**
     * "Target player" read without this effect declaring a target: another effect of the same spell or ability, or
     * the card's target filter, declares it ("target player draws two cards and loses 2 life" with the life loss
     * declaring the target).
     */
    public static PlayerSubject targetPlayerDeclaredElsewhere() {
        return TARGET_PLAYER_DECLARED_ELSEWHERE;
    }

    /** "Each player". */
    public static PlayerSubject eachPlayer() {
        return EACH_PLAYER;
    }

    /** "Each opponent". */
    public static PlayerSubject eachOpponent() {
        return EACH_OPPONENT;
    }

    /** "Each of that player's opponents": every player other than those {@code player} names. */
    public static PlayerSubject opponentsOf(PlayerSubject player) {
        return new PlayerSubject.OpponentsOf(player);
    }

    /** "Its controller", "that creature's controller". */
    public static PlayerSubject controllerOf(PermanentSubject permanent) {
        return new PlayerSubject.ControllerOf(permanent);
    }

    /** "Its owner". */
    public static PlayerSubject ownerOf(PermanentSubject permanent) {
        return new PlayerSubject.OwnerOf(permanent);
    }

    /** "That player": the player the trigger event was about. */
    public static PlayerSubject triggeringPlayer() {
        return TRIGGERING_PLAYER;
    }

    /** "The active player". */
    public static PlayerSubject activePlayer() {
        return ACTIVE_PLAYER;
    }

    /** "Defending player". */
    public static PlayerSubject defendingPlayer() {
        return DEFENDING_PLAYER;
    }

    /** "Enchanted player". */
    public static PlayerSubject enchantedPlayer() {
        return ENCHANTED_PLAYER;
    }

    /** "The chosen player": the player the source chose as it entered. */
    public static PlayerSubject chosenPlayer() {
        return CHOSEN_PLAYER;
    }

    /** "The chosen players": every player the source chose as it entered. */
    public static PlayerSubject chosenPlayers() {
        return CHOSEN_PLAYERS;
    }

    /** The players who said yes to the preceding "each player may" choice of this resolution. */
    public static PlayerSubject acceptedPlayers() {
        return ACCEPTED_PLAYERS;
    }

    /** The players {@code players} names but those {@code excluded} names ("the other chosen player"). */
    public static PlayerSubject except(PlayerSubject players, PlayerSubject excluded) {
        return new PlayerSubject.Except(players, excluded);
    }
}
