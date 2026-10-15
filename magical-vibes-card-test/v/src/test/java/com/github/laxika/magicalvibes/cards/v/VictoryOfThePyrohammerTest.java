package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.g.GarrukPrimalHunter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VictoryOfThePyrohammer.class, ColossalDreadmaw.class, GrizzlyBears.class, GarrukPrimalHunter.class})
class VictoryOfThePyrohammerTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I deals 4 damage to creatures and planeswalkers and preserves creature damage")
    void chapterIDamagesCreaturesAndPlaneswalkersAndPreservesDamage() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Permanent planeswalker = addTestPlaneswalker(player2);

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(saga).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Chapter II deals 1 damage to each creature and planeswalker")
    void chapterIIDealsOneDamage() {
        addSagaWithLore(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = addTestPlaneswalker(player2);

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter III deals 1 damage before the Saga is sacrificed")
    void chapterIIIDealsOneDamageBeforeSacrifice() {
        addSagaWithLore(2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Victory of the Pyrohammer");
        harness.assertInGraveyard(player1, "Victory of the Pyrohammer");
    }

    @Test
    @DisplayName("Damage accumulates across chapters, affects later creatures, and clears after chapter III")
    void damageAccumulatesUntilTheSagaLeaves() {
        addSagaWithLore(0);
        Permanent originalCreature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        advanceToChapter();
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(originalCreature.getMarkedDamage()).isEqualTo(4);

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        advanceToChapter();
        harness.passBothPriorities();
        assertThat(originalCreature.getMarkedDamage()).isEqualTo(5);
        assertThat(laterCreature.getMarkedDamage()).isEqualTo(1);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(originalCreature.getMarkedDamage()).isEqualTo(5);
        assertThat(laterCreature.getMarkedDamage()).isEqualTo(1);

        advanceToChapter();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player1, "Victory of the Pyrohammer");
        assertThat(laterCreature.getMarkedDamage()).isEqualTo(2);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(laterCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(DressDown.class)
    @DisplayName("Removing creature abilities does not disable the Saga's damage preservation")
    void creaturesWithoutAbilitiesStillRetainDamage() {
        addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        advanceToChapter();
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        harness.castFromHand(player1, new DressDown(), "{1}{U}");
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        harness.assertOnBattlefield(player1, "Dress Down");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting the Saga triggers chapter I and kills creatures with lethal damage")
    void enteringTheBattlefieldTriggersChapterI() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new VictoryOfThePyrohammer(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Victory of the Pyrohammer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new VictoryOfThePyrohammer());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addTestPlaneswalker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GarrukPrimalHunter());
        permanent.setCounterCount(CounterType.LOYALTY, 5);
        return permanent;
    }
}
