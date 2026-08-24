package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({Blindblast.class, Forest.class, GrizzlyBears.class})
class BlindblastTest extends BaseCardTest {

    @Test
    void dealsDamagePreventsBlockingAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size() - 1;

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeAfterCast + 1);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void fizzlesWithoutDrawingIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blindblast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        int handSizeBeforeResolution = gd.playerHands.get(player1.getId()).size();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeResolution);
        harness.assertInGraveyard(player1, "Blindblast");
    }
}
