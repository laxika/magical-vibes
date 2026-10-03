package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.cards.s.SummitProwler;
import com.github.laxika.magicalvibes.cards.t.Throttle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathFrenzy.class, Throttle.class, WetlandSambar.class, SummitProwler.class})
class DeathFrenzyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2 and gains life for each creature that dies")
    void weakensAllCreaturesAndGainsLifeForEachDeath() {
        harness.addToBattlefield(player1, new WetlandSambar());
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.setLife(player1, 20);

        castDeathFrenzy();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Wetland Sambar");
        harness.assertNotOnBattlefield(player2, "Wetland Sambar");
    }

    @Test
    @DisplayName("Gains life when a creature dies later in the same turn")
    void triggersForLaterCreatureDeath() {
        harness.addToBattlefield(player1, new SummitProwler());
        harness.setLife(player1, 20);

        castDeathFrenzy();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Throttle()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Summit Prowler"));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("The delayed trigger expires at the end of the turn")
    void delayedTriggerExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new SummitProwler());
        harness.setLife(player1, 20);

        castDeathFrenzy();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Throttle()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Summit Prowler"));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Surviving creatures on both sides recover their power and toughness after cleanup")
    void survivingCreaturesRecoverAfterCleanup() {
        var ownCreature = harness.addToBattlefieldAndReturn(player1, new SummitProwler());
        var opposingCreature = harness.addToBattlefieldAndReturn(player2, new SummitProwler());

        castDeathFrenzy();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering later are not weakened but their deaths still gain life")
    void laterEnteringCreatureIsUnaffectedButDeathTriggers() {
        castDeathFrenzy();
        var creature = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Wetland Sambar");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wetland Sambar");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    private void castDeathFrenzy() {
        harness.setHand(player1, List.of(new DeathFrenzy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
