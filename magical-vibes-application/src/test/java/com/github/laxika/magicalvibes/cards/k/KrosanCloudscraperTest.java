package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KrosanCloudscraper.class)
class KrosanCloudscraperTest extends BaseCardTest {

    @Test
    @DisplayName("Morphs face down and can be turned face up for {7}{G}{G}")
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new KrosanCloudscraper()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent cloudscraper = findPermanent(player1, "Krosan Cloudscraper");
        assertThat(cloudscraper.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 2);
        int cloudscraperIndex = gd.playerBattlefields.get(player1.getId()).indexOf(cloudscraper);
        harness.turnFaceUp(player1, cloudscraperIndex);
        harness.passBothPriorities();

        assertThat(cloudscraper.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Krosan Cloudscraper")
    void decliningUpkeepPaymentSacrificesIt() {
        harness.addToBattlefield(player1, new KrosanCloudscraper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Krosan Cloudscraper");
        harness.assertInGraveyard(player1, "Krosan Cloudscraper");
    }

    @Test
    @DisplayName("Paying {G}{G} keeps Krosan Cloudscraper on the battlefield")
    void payingUpkeepPaymentKeepsItOnBattlefield() {
        harness.addToBattlefield(player1, new KrosanCloudscraper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Krosan Cloudscraper");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
