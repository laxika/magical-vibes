package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Anticognition;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourgeOfTheSkyclaves.class, Anticognition.class})
class ScourgeOfTheSkyclavesTest extends BaseCardTest {

    @Test
    @DisplayName("A kicked cast makes each player lose half their life, rounded up")
    void kickedCastMakesEachPlayerLoseHalfLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 9);
        harness.setHand(player1, List.of(new ScourgeOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Its power and toughness are 20 minus the highest life total")
    void powerAndToughnessTrackHighestLifeTotal() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        harness.castFromHand(player1, new ScourgeOfTheSkyclaves(), "{1}{B}");
        harness.passBothPriorities();

        Permanent scourge = findPermanent(player1, "Scourge of the Skyclaves");
        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(10);

        harness.setLife(player2, 15);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(5);
    }

    @Test
    @DisplayName("An un-kicked cast does not trigger the life loss")
    void unKickedCastDoesNotTriggerLifeLoss() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        harness.castFromHand(player1, new ScourgeOfTheSkyclaves(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("The kicked cast trigger resolves before the creature and lets it survive at high life totals")
    void kickedTriggerResolvesBeforeCreature() {
        harness.setLife(player1, 31);
        harness.setLife(player2, 25);
        harness.setHand(player1, List.of(new ScourgeOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        harness.assertNotOnBattlefield(player1, "Scourge of the Skyclaves");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent scourge = findPermanent(player1, "Scourge of the Skyclaves");
        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(5);
    }

    @Test
    @DisplayName("The cast trigger uses life totals when it resolves")
    void lifeLossUsesResolutionLifeTotals() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ScourgeOfTheSkyclaves()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castKickedCreature(player1, 0);
        harness.setLife(player1, 13);
        harness.setLife(player2, 17);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(6);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Negative power and toughness are preserved in the hand and graveyard")
    void characteristicAbilityWorksOutsideBattlefield() {
        ScourgeOfTheSkyclaves inHand = new ScourgeOfTheSkyclaves();
        ScourgeOfTheSkyclaves inGraveyard = new ScourgeOfTheSkyclaves();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player2, List.of(inGraveyard));
        harness.setLife(player1, 30);
        harness.setLife(player2, 25);

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(-10);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(-10);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(-10);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(-10);

        harness.setLife(player1, 10);
        harness.setLife(player2, 12);

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(8);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(8);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(8);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(8);
    }

    @Test
    @DisplayName("An unkicked creature dies when a player is at twenty life")
    void zeroToughnessCreatureDies() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new ScourgeOfTheSkyclaves(), "{1}{B}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scourge of the Skyclaves");
        harness.assertInGraveyard(player1, "Scourge of the Skyclaves");
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Countering the kicked creature does not counter its life-loss trigger")
    void castTriggerSurvivesCounteredCreature() {
        ScourgeOfTheSkyclaves scourge = new ScourgeOfTheSkyclaves();
        harness.setLife(player1, 19);
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(scourge));
        harness.setHand(player2, List.of(new Anticognition()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castKickedCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, scourge.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Scourge of the Skyclaves");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(9);
        assertThat(gd.getLife(player2.getId())).isEqualTo(7);
        harness.assertNotOnBattlefield(player1, "Scourge of the Skyclaves");
    }
}
