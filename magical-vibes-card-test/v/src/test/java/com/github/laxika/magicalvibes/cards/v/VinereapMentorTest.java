package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VinereapMentor.class, WrathOfGod.class})
class VinereapMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodOnEnter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VinereapMentor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Vinereap Mentor");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Creates a Food token when it dies")
    void createsFoodOnDeath() {
        harness.addToBattlefield(player1, new VinereapMentor());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Vinereap Mentor");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and grants life only on resolution")
    void foodIsSacrificedAsCostAndGainsLifeOnResolution() {
        harness.setHand(player1, List.of(new VinereapMentor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        resolveAllTriggers();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Vinereap Mentor");
    }

    @Test
    @DisplayName("A tapped Food cannot pay its tap cost")
    void tappedFoodCannotBeActivated() {
        harness.setHand(player1, List.of(new VinereapMentor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Simultaneously dying Mentors each create Food for their own controller")
    void simultaneousDeathsCreateFoodForEachController() {
        harness.addToBattlefield(player1, new VinereapMentor());
        harness.addToBattlefield(player2, new VinereapMentor());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Vinereap Mentor");
        harness.assertInGraveyard(player2, "Vinereap Mentor");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
    }
}
