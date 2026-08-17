package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotOfThisWorldTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell targeting a permanent you control")
    void countersSpellTargetingYourPermanent() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new DuskdaleWurm());
        castShockAt(player2, wurm);
        harness.setHand(player1, List.of(new NotOfThisWorld()));

        harness.castInstant(player1, 0, gd.stack.getFirst().getCard().getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Not of This World");
        harness.assertOnBattlefield(player1, "Duskdale Wurm");
    }

    @Test
    @DisplayName("Costs no mana when targeting a spell that targets a creature with power 7 or greater")
    void reducedCostWhenTargetingSpellThatTargetsHighPowerCreature() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new DuskdaleWurm());
        castShockAt(player2, wurm);
        harness.setHand(player1, List.of(new NotOfThisWorld()));

        harness.castInstant(player1, 0, gd.stack.getFirst().getCard().getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not get the reduction when the targeted creature has power less than 7")
    void fullCostWhenTargetingSpellThatTargetsLowPowerCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castShockAt(player2, bears);
        harness.setHand(player1, List.of(new NotOfThisWorld()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, gd.stack.getFirst().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter an ability that targets a permanent you control")
    void countersAbilityTargetingYourPermanent() {
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new DuskdaleWurm());
        harness.activateAbility(player1, 0, null, wurm.getId());
        harness.setHand(player1, List.of(new NotOfThisWorld()));

        harness.castInstant(player1, 0, sorcerer.getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(sorcerer.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Duskdale Wurm");
    }

    @Test
    @DisplayName("Cannot target a spell that targets an opponent's permanent")
    void cannotTargetSpellTargetingOpponentsPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new NotOfThisWorld()));

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castShockAt(Player player, Permanent target) {
        Shock shock = new Shock();
        harness.setHand(player, List.of(shock));
        harness.addMana(player, ManaColor.RED, 1);
        harness.forceActivePlayer(player);
        harness.castInstant(player, 0, target.getId());
        harness.passPriority(player);
    }
}
