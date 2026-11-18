package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShefetArchfiend.class, HillGiant.class, GrizzlyBears.class})
class ShefetArchfiendTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives all other creatures -2/-2 but not Shefet Archfiend")
    void etbDebuffsOtherCreaturesButNotItself() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());

        castShefetArchfiend();
        resolveAllTriggers();

        Permanent ownGiant = findPermanent(player1, "Hill Giant");
        Permanent opponentGiant = findPermanent(player2, "Hill Giant");
        Permanent archfiend = findPermanent(player1, "Shefet Archfiend");

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, archfiend)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, archfiend)).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB debuff wears off at cleanup")
    void etbDebuffWearsOffAtCleanup() {
        harness.addToBattlefield(player2, new HillGiant());

        castShefetArchfiend();
        resolveAllTriggers();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cycling {2} discards Shefet Archfiend and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ShefetArchfiend()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shefet Archfiend");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB kills other creatures with two toughness on both battlefields")
    void etbKillsSmallCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castShefetArchfiend();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Shefet Archfiend");
    }

    @Test
    @DisplayName("Creatures entering after the ETB resolves are not debuffed")
    void laterCreaturesAreNotAffected() {
        castShefetArchfiend();
        resolveAllTriggers();

        Permanent bear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB debuffs another Shefet Archfiend but excludes its own source")
    void anotherArchfiendIsAffected() {
        Permanent earlier = harness.addToBattlefieldAndReturn(player2, new ShefetArchfiend());

        castShefetArchfiend();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, earlier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, earlier)).isEqualTo(3);
        Permanent source = findPermanent(player1, "Shefet Archfiend");
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cycling discards as a cost and does not trigger the battlefield debuff")
    void cyclingDiscardsBeforeDrawingWithoutEtb() {
        harness.setHand(player1, List.of(new ShefetArchfiend()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Shefet Archfiend");
        harness.assertNotInHand(player1, "Shefet Archfiend");
        harness.assertNotInHand(player1, "Hill Giant");

        resolveAllTriggers();

        harness.assertInHand(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    private void castShefetArchfiend() {
        harness.setHand(player1, List.of(new ShefetArchfiend()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
    }
}
