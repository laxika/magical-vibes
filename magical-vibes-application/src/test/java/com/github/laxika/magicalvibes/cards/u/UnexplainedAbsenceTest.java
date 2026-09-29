package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnexplainedAbsence.class, Forest.class, GrizzlyBears.class})
class UnexplainedAbsenceTest extends BaseCardTest {

    @Test
    void exilesUpToOnePermanentPerControllerAndCloaksEachControllersTopCard() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest ownTopCard = new Forest();
        Forest opponentTopCard = new Forest();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));

        castUnexplainedAbsence(List.of(ownPermanent.getId(), opponentPermanent.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentPermanent);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(ownTopCard));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(opponentTopCard));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream())
                .anyMatch(Permanent::isCloaked);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream())
                .anyMatch(Permanent::isCloaked);
    }

    @Test
    void cannotChooseTwoPermanentsControlledByTheSamePlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnexplainedAbsence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    void cannotTargetAland() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new UnexplainedAbsence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    private void castUnexplainedAbsence(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new UnexplainedAbsence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
