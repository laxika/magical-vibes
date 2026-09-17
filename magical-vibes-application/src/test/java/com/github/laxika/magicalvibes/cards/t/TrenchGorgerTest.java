package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrenchGorger.class, Plains.class, Forest.class})
class TrenchGorgerTest extends BaseCardTest {

    private Permanent castAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new TrenchGorger()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Trench Gorger");
    }

    private void chooseTwoLandsAndStop() {
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
    }

    @Test
    @DisplayName("The optional ability exiles lands and sets base power and toughness to that count")
    void exilesLandsAndSetsBasePowerAndToughness() {
        Permanent gorger = castAndResolve(List.of(new Plains(), new Forest(), new Plains()));

        chooseTwoLandsAndStop();

        assertThat(gd.getCardsExiledByPermanent(gorger.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the ability leaves Trench Gorger unchanged")
    void abilityCanBeDeclined() {
        Permanent gorger = castAndResolve(List.of(new Plains(), new Forest()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getCardsExiledByPermanent(gorger.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(6);
    }

    @Test
    @DisplayName("The base power and toughness remain locked if an exiled land leaves exile")
    void basePowerAndToughnessRemainLocked() {
        Permanent gorger = castAndResolve(List.of(new Plains(), new Forest(), new Plains()));

        chooseTwoLandsAndStop();
        UUID exiledId = gd.getCardsExiledByPermanent(gorger.getId()).getFirst().getId();
        gd.removeFromExile(exiledId);

        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(2);
    }
}
