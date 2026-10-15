package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BladeJuggler;
import com.github.laxika.magicalvibes.cards.c.CatacombCrocodile;
import com.github.laxika.magicalvibes.cards.g.Greed;
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

@CardUsed({FontOfAgonies.class, Greed.class, CatacombCrocodile.class, BladeJuggler.class})
class FontOfAgoniesTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever its controller pays life, Font of Agonies gets that many blood counters")
    void payingLifeAddsBloodCounters() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        harness.addToBattlefield(player1, new Greed());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing four blood counters destroys a target creature")
    void abilityDestroysTargetCreature() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        font.setCounterCount(CounterType.BLOOD, 4);
        Permanent target = addCreatureReady(player2, new CatacombCrocodile());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);

        int fontIndex = gd.playerBattlefields.get(player1.getId()).indexOf(font);
        harness.activateAbility(player1, fontIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(font.getCounterCount(CounterType.BLOOD)).isZero();
    }

    @Test
    @DisplayName("Font of Agonies cannot be activated without four blood counters")
    void cannotActivateWithoutFourCounters() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        font.setCounterCount(CounterType.BLOOD, 3);
        Permanent target = addCreatureReady(player2, new CatacombCrocodile());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);

        int fontIndex = gd.playerBattlefields.get(player1.getId()).indexOf(font);
        assertThatThrownBy(() -> harness.activateAbility(player1, fontIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(3);
    }

    @Test
    void countersAreAddedWhenTheTriggerResolvesAndAccumulateAcrossPayments() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        harness.addToBattlefield(player1, new Greed());
        harness.setLibrary(player1, List.of(new CatacombCrocodile(), new CatacombCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, null, null);
        assertThat(font.getCounterCount(CounterType.BLOOD)).isZero();
        harness.passBothPriorities();
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(2);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, null);
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(4);
    }

    @Test
    void opponentsLifePaymentDoesNotAddCounters() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        harness.addToBattlefield(player2, new Greed());
        harness.setLibrary(player2, List.of(new CatacombCrocodile()));
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(font.getCounterCount(CounterType.BLOOD)).isZero();
    }

    @Test
    void damageToControllerDoesNotCountAsPayingLife() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new CatacombCrocodile()));

        harness.enterBattlefieldAndReturn(player1, new BladeJuggler());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(font.getCounterCount(CounterType.BLOOD)).isZero();
    }

    @Test
    void activationPaysExactlyFourCountersBeforeResolutionAndCanTargetOwnCreature() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        font.setCounterCount(CounterType.BLOOD, 6);
        Permanent target = addCreatureReady(player1, new CatacombCrocodile());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    void cannotTargetAnEnchantment() {
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfAgonies());
        font.setCounterCount(CounterType.BLOOD, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FontOfAgonies());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(4);
    }
}
