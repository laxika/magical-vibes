package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TreasonousOgre.class)
class TreasonousOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 3 life to add {R}")
    void paysLifeForRedMana() {
        addCreatureReady(player1, new TreasonousOgre());
        GameData gd = harness.getGameData();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the activation cost without 3 life")
    void cannotPayActivationCostWithoutEnoughLife() {
        addCreatureReady(player1, new TreasonousOgre());
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Dethrone puts a +1/+1 counter on the attacking Ogre")
    void dethroneTriggersWhenAttackingPlayerWithMostLife() {
        Permanent ogre = addCreatureReady(player1, new TreasonousOgre());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ogre)));
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana ability can be used repeatedly while tapped and summoning sick")
    void manaAbilityWorksWhileTappedAndSummoningSick() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new TreasonousOgre());
        ogre.setSummoningSick(true);
        ogre.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(ogre.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejected life payment grants no mana and loses no life")
    void rejectedPaymentDoesNotChangeLifeOrMana() {
        harness.addToBattlefield(player1, new TreasonousOgre());
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dethrone triggers when defending player has strictly most life")
    void dethroneTriggersWhenDefenderHasStrictlyMostLife() {
        Permanent ogre = addCreatureReady(player1, new TreasonousOgre());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dethrone does not trigger when the attacking player has more life")
    void dethroneDoesNotTriggerAgainstLowerLifePlayer() {
        Permanent ogre = addCreatureReady(player1, new TreasonousOgre());
        harness.setLife(player1, 20);
        harness.setLife(player2, 15);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dethrone does not recheck life totals when its trigger resolves")
    void dethroneDoesNotRecheckLifeTotalsAtResolution() {
        Permanent ogre = addCreatureReady(player1, new TreasonousOgre());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.setLife(player2, 10);
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
