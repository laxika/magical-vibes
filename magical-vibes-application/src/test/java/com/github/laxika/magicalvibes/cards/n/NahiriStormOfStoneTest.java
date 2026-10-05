package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IroncladKrovod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NahiriStormOfStone.class, DarksteelAxe.class, GrizzlyBears.class, HillGiant.class,
        IroncladKrovod.class})
class NahiriStormOfStoneTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, your creatures have first strike")
    void grantsFirstStrikeToOwnCreaturesDuringYourTurn() {
        addReadyNahiri(player1, 4);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("During your turn, equip abilities you activate cost one less")
    void reducesEquipCostDuringYourTurn() {
        addReadyNahiri(player1, 4);
        Permanent axe = addCreatureReady(player1, new DarksteelAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(axe),
                0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Minus X deals X damage to a tapped creature")
    void minusXDamagesTappedCreature() {
        Permanent nahiri = addReadyNahiri(player1, 4);
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.tap();

        harness.activateAbility(player1, 0, 0, 3, target.getId());
        harness.passBothPriorities();

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Minus X cannot target an untapped creature")
    void minusXCannotTargetUntappedCreature() {
        addReadyNahiri(player1, 4);
        Permanent target = addCreatureReady(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    void zeroXCanTargetOwnTappedCreatureWithoutDealingDamage() {
        Permanent nahiri = addReadyNahiri(player1, 4);
        Permanent target = addCreatureReady(player1, new IroncladKrovod());
        target.tap();

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Ironclad Krovod");
    }

    @Test
    void untappingTargetBeforeResolutionMakesItIllegal() {
        Permanent nahiri = addReadyNahiri(player1, 4);
        Permanent target = addCreatureReady(player2, new IroncladKrovod());
        target.tap();

        harness.activateAbility(player1, 0, 0, 3, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Ironclad Krovod");
    }

    @Test
    void spendingAllLoyaltyStillDealsChosenDamageAfterNahiriLeaves() {
        addReadyNahiri(player1, 4);
        Permanent target = addCreatureReady(player2, new IroncladKrovod());
        target.tap();

        harness.activateAbility(player1, 0, 0, 4, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nahiri, Storm of Stone");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Ironclad Krovod");
    }

    @Test
    void cannotSpendMoreLoyaltyThanAvailable() {
        Permanent nahiri = addReadyNahiri(player1, 4);
        Permanent target = addCreatureReady(player2, new IroncladKrovod());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 5, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceOpponentsEquipCost() {
        addReadyNahiri(player1, 4);
        Permanent axe = addCreatureReady(player2, new DarksteelAxe());
        Permanent target = addCreatureReady(player2, new IroncladKrovod());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(target.getId());
    }

    private Permanent addReadyNahiri(Player player, int loyalty) {
        Permanent nahiri = addCreatureReady(player, new NahiriStormOfStone());
        nahiri.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return nahiri;
    }

}
