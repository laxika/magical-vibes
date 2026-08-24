package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({VoldarenThrillseeker.class, GrizzlyBears.class})
class VoldarenThrillseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts two +1/+1 counters on another creature and grants the sacrifice damage ability")
    void backsUpAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        castVoldarenThrillseeker();
        resolveEtbTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        int opponentLife = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        harness.activateAbility(player1, bearsIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 4);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Backup targeting Voldaren Thrillseeker only puts the counters on it")
    void backsUpItself() {
        Permanent thrillseeker = castVoldarenThrillseeker();
        resolveEtbTargeting(thrillseeker);

        assertThat(thrillseeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Backup's granted sacrifice damage ability expires at the end of the turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castVoldarenThrillseeker();
        resolveEtbTargeting(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> harness.activateAbility(player1, bearsIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent castVoldarenThrillseeker() {
        harness.setHand(player1, List.of(new VoldarenThrillseeker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof VoldarenThrillseeker)
                .findFirst()
                .orElseThrow();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
