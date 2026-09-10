package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaZarOfTheSavageLand.class, Forest.class})
class KaZarOfTheSavageLandTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a legendary Zabu token that grows when a land enters")
    void createsZabuThatGrowsFromLandfall() {
        harness.setHand(player1, List.of(new KaZarOfTheSavageLand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent zabu = findPermanent(player1, "Zabu");
        assertThat(zabu.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(zabu.getEffectivePower()).isEqualTo(2);
        assertThat(zabu.getEffectiveToughness()).isEqualTo(2);

        playForest();
        harness.passBothPriorities();

        assertThat(zabu.getEffectivePower()).isEqualTo(3);
        assertThat(zabu.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows playing a land from the top of the library")
    void playsLandFromTopOfLibrary() {
        harness.addToBattlefield(player1, new KaZarOfTheSavageLand());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    private void playForest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
    }
}
