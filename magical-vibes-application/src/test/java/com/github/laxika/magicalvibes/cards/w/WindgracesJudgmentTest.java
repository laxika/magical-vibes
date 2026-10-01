package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindgracesJudgment.class, GrizzlyBears.class, Plains.class})
class WindgracesJudgmentTest extends BaseCardTest {

    @Test
    void destroysOneTargetPerOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void canChooseNoTargets() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivor);
    }

    @Test
    void rejectsTwoTargetsControlledByTheSameOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetLandsOrOwnPermanents() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);

        prepareCast();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new WindgracesJudgment()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
