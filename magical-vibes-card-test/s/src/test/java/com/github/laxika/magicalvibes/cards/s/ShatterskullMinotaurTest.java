package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatterskullMinotaur.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, SoulWarden.class, StoneworkPackbeast.class})
class ShatterskullMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces the generic cost by four")
    void fullPartyReducesCostByFour() {
        addFullParty();
        harness.castFromHand(player1, new ShatterskullMinotaur(), "{R}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without a party, the full generic cost is required")
    void withoutPartyRequiresFullGenericCost() {
        harness.setHand(player1, List.of(new ShatterskullMinotaur()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack immediately after being cast")
    void canAttackImmediatelyAfterBeingCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ShatterskullMinotaur(), "{4}{R}{R}");
        harness.passBothPriorities();

        assertThatCode(() -> declareAttackers(List.of(0)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    @DisplayName("Each multitype creature fills only one role and party size stops at four")
    void multitypeCreaturesReduceCostByAtMostFour(int creatureCount) {
        for (int i = 0; i < creatureCount; i++) {
            harness.addToBattlefield(player1, new StoneworkPackbeast());
        }
        int genericCost = 4 - Math.min(creatureCount, 4);

        harness.castFromHand(player1, new ShatterskullMinotaur(),
                "{" + genericCost + "}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Warriors fill only one party role")
    void duplicateRolesDoNotIncreaseReduction() {
        harness.addToBattlefield(player1, new ShatterskullMinotaur());
        harness.addToBattlefield(player1, new ShatterskullMinotaur());

        harness.castFromHand(player1, new ShatterskullMinotaur(), "{3}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A multitype creature takes an unfilled role to maximize party size")
    void multitypeCreatureFillsMissingRole() {
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        harness.addToBattlefield(player1, new ShatterskullMinotaur());

        harness.castFromHand(player1, new ShatterskullMinotaur(), "{2}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's party does not reduce the casting cost")
    void opponentsPartyDoesNotReduceCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new StoneworkPackbeast());
        }
        harness.setHand(player1, List.of(new ShatterskullMinotaur()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Even a full party cannot reduce the two red mana symbols")
    void fullPartyDoesNotReduceColoredCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new StoneworkPackbeast());
        }
        harness.setHand(player1, List.of(new ShatterskullMinotaur()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }
}
