package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RewardTheFaithful;
import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GildedLight.class, RewardTheFaithful.class, SilverKnight.class})
class GildedLightTest extends BaseCardTest {

    @Test
    void controllerGainsShroudUntilEndOfTurn() {
        harness.addToBattlefield(player1, new SilverKnight());
        harness.setHand(player1, List.of(new GildedLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        harness.assertLife(player1, 22);
    }

    @Test
    void cyclingDiscardsGildedLightAndDrawsACard() {
        harness.setHand(player1, List.of(new GildedLight()));
        harness.setLibrary(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Gilded Light");
        harness.assertNotInHand(player1, "Gilded Light");
        harness.assertNotInHand(player1, "Reward the Faithful");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gilded Light");
        harness.assertInHand(player1, "Reward the Faithful");
    }

    @Test
    void shroudMakesAnAlreadyCastSpellLoseItsOnlyTarget() {
        harness.addToBattlefield(player2, new SilverKnight());
        harness.setHand(player2, List.of(new RewardTheFaithful()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, List.of(player1.getId()));

        harness.setHand(player1, List.of(new GildedLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Reward the Faithful");
    }

    @Test
    void shroudDoesNotStopASpellFromAffectingItsRemainingLegalTarget() {
        harness.addToBattlefield(player2, new SilverKnight());
        harness.setHand(player2, List.of(new RewardTheFaithful()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, List.of(player1.getId(), player2.getId()));

        harness.setHand(player1, List.of(new GildedLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player2, "Reward the Faithful");
    }

    @Test
    void shroudBlocksOpponentsButDoesNotProtectTheOtherPlayer() {
        harness.setHand(player1, List.of(new GildedLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.addToBattlefield(player2, new SilverKnight());
        harness.setHand(player2, List.of(new RewardTheFaithful()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");

        harness.castAndResolveInstant(player2, 0, List.of(player2.getId()));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    void cyclingDoesNotGrantShroud() {
        harness.addToBattlefield(player1, new SilverKnight());
        harness.setHand(player1, List.of(new GildedLight()));
        harness.setLibrary(player1, List.of(new RewardTheFaithful()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, List.of(player1.getId()));

        harness.assertLife(player1, 22);
    }
}
