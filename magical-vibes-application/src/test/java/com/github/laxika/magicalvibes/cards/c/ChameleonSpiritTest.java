package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeepwoodDrummer;
import com.github.laxika.magicalvibes.cards.d.DistortingLens;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChameleonSpirit.class, CloudSprite.class, CoilingOracle.class, Cowardice.class,
        DeepwoodDrummer.class, DistortingLens.class, RishadanPort.class})
class ChameleonSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color sets power and toughness to matching opponent permanents")
    void countsMatchingOpponentPermanentsAfterChoosingColor() {
        harness.addToBattlefield(player2, new CloudSprite());
        harness.addToBattlefield(player2, new CoilingOracle());
        harness.addToBattlefield(player2, new Cowardice());
        harness.addToBattlefield(player2, new DeepwoodDrummer());
        harness.addToBattlefield(player1, new CloudSprite());

        harness.castFromHand(player1, new ChameleonSpirit(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        Permanent spirit = findPermanent(player1, "Chameleon Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update as matching opponent permanents change")
    void updatesWhenMatchingOpponentPermanentsChange() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new ChameleonSpirit());
        spirit.setChosenColor(CardColor.BLUE);

        Permanent firstBlue = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);

        harness.addToBattlefield(player2, new CloudSprite());
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(firstBlue);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("No chosen color means no matching permanents")
    void noChosenColorMeansZeroPowerAndToughness() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new ChameleonSpirit());
        harness.addToBattlefield(player2, new CloudSprite());

        assertThat(gqs.getEffectivePower(gd, spirit)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isZero();
    }

    @Test
    @DisplayName("Choosing a color with no matching opponent permanents gives zero power and toughness")
    void chosenColorWithNoMatchingOpponentPermanentsIsZero() {
        harness.addToBattlefield(player2, new DeepwoodDrummer());

        harness.castFromHand(player1, new ChameleonSpirit(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.assertNotOnBattlefield(player1, "Chameleon Spirit");
        harness.assertInGraveyard(player1, "Chameleon Spirit");
    }

    @Test
    @DisplayName("A permanent changed to the chosen color increases power and toughness")
    void countsPermanentChangedToChosenColor() {
        harness.addToBattlefield(player1, new DistortingLens());
        harness.addToBattlefield(player2, new CloudSprite());
        Permanent drummer = harness.addToBattlefieldAndReturn(player2, new DeepwoodDrummer());
        harness.castFromHand(player1, new ChameleonSpirit(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent spirit = findPermanent(player1, "Chameleon Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, drummer.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing the last matching color puts Chameleon Spirit into the graveyard")
    void diesWhenLastMatchingPermanentChangesColor() {
        harness.addToBattlefield(player1, new DistortingLens());
        Permanent sprite = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        harness.castFromHand(player1, new ChameleonSpirit(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.assertOnBattlefield(player1, "Chameleon Spirit");

        harness.activateAbility(player1, 0, 0, null, sprite.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.assertNotOnBattlefield(player1, "Chameleon Spirit");
        harness.assertInGraveyard(player1, "Chameleon Spirit");
    }

    @Test
    @DisplayName("Colorless lands do not count, but lands changed to the chosen color do")
    void countsColoredLandsAndIgnoresColorlessPermanents() {
        harness.addToBattlefield(player1, new DistortingLens());
        harness.addToBattlefield(player2, new CloudSprite());
        harness.addToBattlefield(player2, new DistortingLens());
        Permanent port = harness.addToBattlefieldAndReturn(player2, new RishadanPort());
        harness.castFromHand(player1, new ChameleonSpirit(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent spirit = findPermanent(player1, "Chameleon Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, port.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }
}
