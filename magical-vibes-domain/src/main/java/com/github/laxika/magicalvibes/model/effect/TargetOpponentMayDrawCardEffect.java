package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Target opponent may draw a card" on an activated ability that also targets a creature (Soldevi Heretic): the
 * opponent is the ability's own player target, {@code opponent} (its target group, {@code Subjects.targetPlayer(1)}),
 * which the ability's target filters declare. Declaring {@link TargetSpec#NONE} keeps the effect out of the entry's
 * first target slot, which the prevention half's creature takes.
 * <p>
 * {@code TargetOpponentMayDrawCardEffectHandler} offers the draw to that opponent as the ability resolves, so the "may"
 * is theirs (CR 608.2d). As with any targets, an opponent who became an illegal target is offered nothing, and the
 * ability still resolves for the other target (CR 608.2b). A spell or ability
 * whose only target is the opponent is
 * {@code MayEffect(DrawCardEffect(1, targetPlayerDeclaredElsewhere()), ..., MayChoicePlayer.TARGET_PLAYER)}
 * (Phelddagrif, Tataru Taru).
 *
 * @param opponent the targeted opponent
 */
public record TargetOpponentMayDrawCardEffect(PlayerSubject opponent) implements CardEffect {

    public TargetOpponentMayDrawCardEffect {
        Objects.requireNonNull(opponent, "opponent");
    }
}
