package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeepwoodDrummer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChameleonSpirit.class, CloudSprite.class, CoilingOracle.class, Cowardice.class,
        DeepwoodDrummer.class})
class ChameleonSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color sets power and toughness to matching opponent permanents")
    void countsMatchingOpponentPermanentsAfterChoosingColor() {
        harness.addToBattlefield(player2, new CloudSprite());
        harness.addToBattlefield(player2, new CoilingOracle());
        harness.addToBattlefield(player2, new Cowardice());
        harness.addToBattlefield(player2, new DeepwoodDrummer());
        harness.addToBattlefield(player1, new CloudSprite());

        harness.setHand(player1, List.of(new ChameleonSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new ChameleonSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.assertNotOnBattlefield(player1, "Chameleon Spirit");
        harness.assertInGraveyard(player1, "Chameleon Spirit");
    }
}
