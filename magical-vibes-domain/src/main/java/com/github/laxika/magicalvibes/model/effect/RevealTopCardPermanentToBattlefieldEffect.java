package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[Player] reveals the top card of their library. If it's a permanent card, they put it onto the battlefield": each
 * player {@code players} names reveals their top card, and those that are permanent cards enter the battlefield together
 * under their owners' control through the entry batch; a nonpermanent card stays on top. {@code you()} by default; "each
 * player who sacrificed a permanent this way" is {@code playersThisWay(StepKind.SACRIFICE)} (Vaevictis Asmadi, the
 * Dire).
 *
 * @param players who reveal; never a target
 */
public record RevealTopCardPermanentToBattlefieldEffect(PlayerSubject players) implements CardEffect {

    public RevealTopCardPermanentToBattlefieldEffect {
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who reveal are not targeted: " + players);
        }
    }

    public RevealTopCardPermanentToBattlefieldEffect() {
        this(Subjects.you());
    }
}
