package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanewhipPunisher.class, Manalith.class})
class BanewhipPunisherTest extends BaseCardTest {

    @Test
    void enteringCanPutCounterOnOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BanewhipPunisher());
        harness.setHand(player1, List.of(new BanewhipPunisher()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void enteringCanDeclineCounterOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BanewhipPunisher());
        harness.enterBattlefieldAndReturn(player1, new BanewhipPunisher());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringCanPutCounterOnItself() {
        Permanent punisher = harness.enterBattlefieldAndReturn(player1, new BanewhipPunisher());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, punisher.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(punisher.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(punisher);
    }

    @Test
    void activationSacrificesImmediatelyAndDestroysCounterBearingCreatureOnResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BanewhipPunisher());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BanewhipPunisher());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInGraveyard(player1, "Banewhip Punisher");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Banewhip Punisher");
    }

    @Test
    void activationCanDestroyAnotherCreatureYouControl() {
        harness.addToBattlefield(player1, new BanewhipPunisher());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BanewhipPunisher());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void activationRejectsCreatureWithoutMinusCounterBeforeSacrificing() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BanewhipPunisher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BanewhipPunisher());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresBlackManaBeforeSacrificing() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BanewhipPunisher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BanewhipPunisher());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRejectsNoncreatureEvenWithMinusCounter() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BanewhipPunisher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Manalith());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationCanTargetItselfAndIsAlreadySacrificedWhenAbilityResolves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BanewhipPunisher());
        source.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Banewhip Punisher");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetLosingLastMinusCounterMakesAbilityFailToResolve() {
        harness.addToBattlefield(player1, new BanewhipPunisher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BanewhipPunisher());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInGraveyard(player2, "Banewhip Punisher");
        harness.assertInGraveyard(player1, "Banewhip Punisher");
        assertThat(gd.stack).isEmpty();
    }
}
