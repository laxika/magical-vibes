package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Prompts the player carried by the stack entry to choose one of the supplied modal options. When
 * {@code villainousChoice} is set, the choice is "faces a villainous choice" so replacement effects
 * that repeat it (The Valeyard) apply.
 *
 * <p>With {@code players} ({@link #villainousChoiceFacedBy}) each of those players faces it, one at a time in APNAP
 * order, all of one player's chosen option performed before the next one chooses (CR 701.55d). "That player" in an
 * option is then {@code Subjects.targetPlayerDeclaredElsewhere()}: the entry carries the player facing it.
 *
 * @param options          the options
 * @param targetsPlayer    whether the player who chooses is the effect's target
 * @param villainousChoice whether it is a villainous choice
 * @param players          who each face the villainous choice; {@code null} for the carried player
 * @param facingPlayerId   internal: the one of them facing it in this step; {@code null} on a card
 */
public record TargetPlayerChoosesOneEffect(List<ChooseOneEffect.ChooseOneOption> options, boolean targetsPlayer,
                                           boolean villainousChoice, PlayerSubject players, UUID facingPlayerId)
        implements CardEffect {

    public TargetPlayerChoosesOneEffect(List<ChooseOneEffect.ChooseOneOption> options) {
        this(options, false, false, null, null);
    }

    public static TargetPlayerChoosesOneEffect forTargetedPlayer(List<ChooseOneEffect.ChooseOneOption> options) {
        return new TargetPlayerChoosesOneEffect(options, true, false, null, null);
    }

    /** The carried (e.g. damaged) player faces a villainous choice between the supplied options. */
    public static TargetPlayerChoosesOneEffect forTargetedPlayerVillainousChoice(
            List<ChooseOneEffect.ChooseOneOption> options) {
        return new TargetPlayerChoosesOneEffect(options, true, true, null, null);
    }

    /** "Each opponent faces a villainous choice": each of {@code players} faces it in turn. */
    public static TargetPlayerChoosesOneEffect villainousChoiceFacedBy(
            PlayerSubject players, List<ChooseOneEffect.ChooseOneOption> options) {
        return new TargetPlayerChoosesOneEffect(options, false, true, players, null);
    }

    public TargetPlayerChoosesOneEffect {
        options = List.copyOf(options);
        if (players != null && (!villainousChoice || targetsPlayer || players.declaresTarget())) {
            throw new IllegalArgumentException("Only an untargeted villainous choice is faced by several players");
        }
    }

    /** This villainous choice as {@code playerId}'s own step. */
    public TargetPlayerChoosesOneEffect facedBy(UUID playerId) {
        return new TargetPlayerChoosesOneEffect(options, false, true, null, playerId);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
