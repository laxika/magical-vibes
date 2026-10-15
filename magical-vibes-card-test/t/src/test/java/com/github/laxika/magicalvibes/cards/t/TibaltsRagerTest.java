package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CentaurNurturer;
import com.github.laxika.magicalvibes.cards.c.CharmedStray;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TibaltsRager.class, CentaurNurturer.class, CharmedStray.class, JayaVeneratedFiremage.class})
class TibaltsRagerTest extends BaseCardTest {

    @Test
    @DisplayName("When Tibalt's Rager dies, it deals 1 damage to a chosen player")
    void deathTriggerDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new TibaltsRager());
        harness.setLife(player2, 20);
        killRagerInCombat();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("When Tibalt's Rager dies, it deals 1 damage to a chosen creature")
    void deathTriggerDealsDamageToCreature() {
        harness.addToBattlefield(player1, new TibaltsRager());
        harness.addToBattlefield(player2, new CharmedStray());
        UUID strayId = harness.getPermanentId(player2, "Charmed Stray");
        killRagerInCombat();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, strayId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Charmed Stray");
    }

    @Test
    @DisplayName("Activating Tibalt's Rager gives it +2/+0 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent rager = addCreatureReady(player1, new TibaltsRager());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rager.getPowerModifier()).isEqualTo(2);
        assertThat(rager.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Tibalt's Rager's activated ability wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent rager = addCreatureReady(player1, new TibaltsRager());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rager.getPowerModifier()).isEqualTo(0);
    }

    @Test
    void deathTriggerCanDamageItsController() {
        harness.addToBattlefield(player1, new TibaltsRager());
        harness.setLife(player1, 20);
        killRagerInCombat();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void deathTriggerCanDamagePlaneswalker() {
        harness.addToBattlefield(player1, new TibaltsRager());
        Permanent jaya = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        killRagerInCombat();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, jaya.getId());
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void multipleActivationsStackWhileSummoningSickAndTapped() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new TibaltsRager());
        rager.setSummoningSick(true);
        rager.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rager.getPowerModifier()).isEqualTo(4);
        assertThat(rager.getToughnessModifier()).isZero();
        assertThat(rager.isTapped()).isTrue();
    }

    private void killRagerInCombat() {
        Permanent rager = findPermanent(player1, "Tibalt's Rager");
        rager.setSummoningSick(false);
        rager.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CentaurNurturer());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
