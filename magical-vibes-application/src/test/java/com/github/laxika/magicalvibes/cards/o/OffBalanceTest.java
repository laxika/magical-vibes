package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.k.KillSwitch;
import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OffBalance.class, RootwaterCommando.class, KillSwitch.class})
class OffBalanceTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature cannot attack or block this turn")
    void preventsAttackingAndBlocking() {
        Permanent target = addCreatureReady(player2, new RootwaterCommando());
        Permanent unaffected = addCreatureReady(player2, new RootwaterCommando());
        harness.setHand(player1, List.of(new OffBalance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isCantAttackThisTurn()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(als.canAttack(gd, target, player2.getId())).isFalse();
        assertThat(bls.canBlock(gd, target)).isFalse();
        assertThat(als.canAttack(gd, unaffected, player2.getId())).isTrue();
        assertThat(bls.canBlock(gd, unaffected)).isTrue();
    }

    @Test
    @DisplayName("Attack and block restrictions expire at end of turn")
    void restrictionsExpireAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new RootwaterCommando());
        harness.setHand(player1, List.of(new OffBalance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(als.canAttack(gd, target, player2.getId())).isFalse();
        assertThat(bls.canBlock(gd, target)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, target, player2.getId())).isTrue();
        assertThat(bls.canBlock(gd, target)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new KillSwitch());
        harness.setHand(player1, List.of(new OffBalance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
