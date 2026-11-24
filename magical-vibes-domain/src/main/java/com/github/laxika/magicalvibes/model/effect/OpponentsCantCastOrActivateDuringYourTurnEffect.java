package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.DuringCombat;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerCouldCastSorcery;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerTurn;

import java.util.Objects;

/**
 * Static cast and activation lock: while {@code condition} is met for a player {@code players} names, that player can't
 * do what {@code spells} and {@code abilities} say. A "can't" beats every permission (CR 101.2): nothing it prohibits
 * can begin to be cast (CR 601.3) or activated (CR 602.5), during a resolution too.
 *
 * <ul>
 *   <li>"During your turn, your opponents can't cast spells or activate abilities of artifacts, creatures, or
 *       enchantments" (Grand Abolisher, Myrel): {@link #OpponentsCantCastOrActivateDuringYourTurnEffect()}.</li>
 *   <li>"Your opponents can't cast spells during your turn" (Voice of Victory, Dragonlord Dromoka):
 *       {@code (false)}.</li>
 *   <li>"[Players / your opponents] can't cast spells during combat" (Basandra, Marisi):
 *       {@code (players, castingSpells(), null, new DuringCombat())}; "during combat, players can't cast instant spells
 *       or activate abilities that aren't mana abilities" (Hand to Hand) adds {@code castingSpellsOf(INSTANT)} and
 *       {@code activatingNonManaAbilities()}.</li>
 *   <li>"Players [or you] can cast spells [and activate abilities] only during their own turns" (Dosan, Fires of
 *       Invention, City of Solitude): {@link #onlyDuringTheirOwnTurns}.</li>
 *   <li>"Each opponent can cast spells only any time they could cast a sorcery" (Teferi): {@link #onlyAtSorceryTiming}.
 *       This is timing (CR 307.5): it holds the player to their main phase with an empty stack, and an effect that
 *       stops casting sorceries doesn't touch it.</li>
 *   <li>"Players can't cast instant spells unless a spell or ability is on the stack" (Priority Avenger):
 *       {@code (eachPlayer(), castingSpellsOf(INSTANT), null, new StackIsEmpty())}.</li>
 * </ul>
 *
 * <p>Read by {@code GameQueryService.isPlayerRestricted} beside the resolved restrictions; a face-down permanent or one
 * that lost its abilities restricts nobody (CR 708.2a, 613.1f).
 *
 * @param players   who is restricted, judged as the restriction is checked: {@code you()}, {@code eachPlayer()} or
 *                  {@code eachOpponent()}
 * @param spells    the spells they can't cast: a zone-free {@link PlayerRestriction.Kind#CAST_SPELLS} restriction, or
 *                  {@code null} for none
 * @param abilities the abilities they can't activate: {@link PlayerRestriction#activatingAbilities()},
 *                  {@link PlayerRestriction#activatingAbilitiesOf} or
 *                  {@link PlayerRestriction#activatingNonManaAbilities()}; {@code null} for none
 * @param condition when the lock holds, judged for each restricted player as the context's target
 *                  ({@code ControllerTurn} is the source controller's turn, {@code TargetPlayerTurn} the restricted
 *                  player's); {@code null} for always
 */
public record OpponentsCantCastOrActivateDuringYourTurnEffect(PlayerSubject players, PlayerRestriction spells,
                                                              PlayerRestriction abilities, Condition condition)
        implements CardEffect {

    private static final Condition NOT_AT_SORCERY_TIMING = new NotCondition(new TargetPlayerCouldCastSorcery());
    private static final Condition NOT_THEIR_TURN = new NotCondition(new TargetPlayerTurn());

    public OpponentsCantCastOrActivateDuringYourTurnEffect {
        Objects.requireNonNull(players, "players");
        if (!(players instanceof PlayerSubject.Each || players instanceof PlayerSubject.You)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
        if (spells == null && abilities == null) {
            throw new IllegalArgumentException("A cast lock restricts spells, abilities or both");
        }
        if (spells != null && (spells.kind() != PlayerRestriction.Kind.CAST_SPELLS || spells.isZoneBound())) {
            throw new IllegalArgumentException("A cast lock's spells are a zone-free spell restriction: " + spells);
        }
        if (abilities != null && abilities.kind() != PlayerRestriction.Kind.ACTIVATE_ABILITIES
                && abilities.kind() != PlayerRestriction.Kind.ACTIVATE_NON_MANA_ABILITIES) {
            throw new IllegalArgumentException("A cast lock's abilities are an ability restriction: " + abilities);
        }
    }

    /**
     * "During your turn, your opponents can't cast spells or activate abilities of artifacts, creatures, or
     * enchantments", or without the abilities "your opponents can't cast spells during your turn".
     */
    public OpponentsCantCastOrActivateDuringYourTurnEffect(boolean restrictsActivatedAbilities) {
        this(Subjects.eachOpponent(), PlayerRestriction.castingSpells(),
                restrictsActivatedAbilities ? PlayerRestriction.activatingAbilitiesOf(
                        CardType.ARTIFACT, CardType.CREATURE, CardType.ENCHANTMENT) : null,
                new ControllerTurn());
    }

    /** Grand Abolisher's "during your turn, your opponents can't cast spells or activate abilities of …". */
    public OpponentsCantCastOrActivateDuringYourTurnEffect() {
        this(true);
    }

    /**
     * "[Players] can cast spells [and activate abilities] only during their own turns": the lock holds outside the
     * restricted player's turn.
     *
     * @param abilities {@link PlayerRestriction#activatingAbilities()} for "and activate abilities", or {@code null}
     */
    public static OpponentsCantCastOrActivateDuringYourTurnEffect onlyDuringTheirOwnTurns(PlayerSubject players,
                                                                                        PlayerRestriction abilities) {
        return new OpponentsCantCastOrActivateDuringYourTurnEffect(players, PlayerRestriction.castingSpells(), abilities,
                NOT_THEIR_TURN);
    }

    /** "[Players] can cast spells only any time they could cast a sorcery" (CR 307.5). */
    public static OpponentsCantCastOrActivateDuringYourTurnEffect onlyAtSorceryTiming(PlayerSubject players) {
        return new OpponentsCantCastOrActivateDuringYourTurnEffect(players, PlayerRestriction.castingSpells(), null,
                NOT_AT_SORCERY_TIMING);
    }

    /** "[Players] can't cast spells during combat". */
    public static OpponentsCantCastOrActivateDuringYourTurnEffect duringCombat(PlayerSubject players) {
        return new OpponentsCantCastOrActivateDuringYourTurnEffect(players, PlayerRestriction.castingSpells(), null,
                new DuringCombat());
    }

    /**
     * Whether this holds its players to sorcery timing ({@link #onlyAtSorceryTiming}). Such a player can't cast a spell
     * while another spell or ability resolves, as rebound and suspend would let them: that is never a time they could
     * cast a sorcery (CR 608.2g; Teferi, Mage of Zhalfir's ruling).
     */
    public boolean holdsToSorceryTiming() {
        return NOT_AT_SORCERY_TIMING.equals(condition) && spells != null && spells.stopsEverySpell();
    }

    /** When the lock holds, as the restricted player is told: " during combat", " outside your own turn". */
    public String whenRestricted() {
        if (condition == null) {
            return "";
        }
        return switch (condition) {
            case ControllerTurn ignored -> " during your opponent's turn";
            case DuringCombat ignored -> " during combat";
            case NotCondition(TargetPlayerTurn ignored) -> " outside your own turn";
            case NotCondition(TargetPlayerCouldCastSorcery ignored) -> " except any time you could cast a sorcery";
            default -> " now";
        };
    }
}
