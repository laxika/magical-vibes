package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BristlingBackwoods;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpinewoodsArmadillo.class, BristlingBackwoods.class, Forest.class, ShootTheSheriff.class})
class SpinewoodsArmadilloTest extends BaseCardTest {

    @Test
    @DisplayName("The hand ability discards the card and offers basic lands and Deserts")
    void handAbilityOffersBasicLandsAndDeserts() {
        SpinewoodsArmadillo armadillo = new SpinewoodsArmadillo();
        Forest forest = new Forest();
        BristlingBackwoods desert = new BristlingBackwoods();
        harness.setHand(player1, List.of(armadillo));
        harness.setLibrary(player1, List.of(forest, desert, new SpinewoodsArmadillo()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(armadillo);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, desert);
    }

    @Test
    @DisplayName("Choosing a basic land or Desert puts it into hand and gains 3 life")
    void choosingLandGainsLife() {
        SpinewoodsArmadillo armadillo = new SpinewoodsArmadillo();
        Card desert = new BristlingBackwoods();
        harness.setHand(player1, List.of(armadillo));
        harness.setLibrary(player1, List.of(new Forest(), desert, new SpinewoodsArmadillo()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).contains(desert);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void choosingBasicLandGainsLifeAndRevealsIt() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SpinewoodsArmadillo()));
        harness.setLibrary(player1, List.of(forest, new BristlingBackwoods()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void failingToFindStillGainsLife() {
        Forest forest = new Forest();
        BristlingBackwoods desert = new BristlingBackwoods();
        harness.setHand(player1, List.of(new SpinewoodsArmadillo()));
        harness.setLibrary(player1, List.of(forest, desert));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, desert);
        harness.assertLife(player1, 23);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryStillGainsLife() {
        harness.setHand(player1, List.of(new SpinewoodsArmadillo()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void libraryWithNoMatchingCardsStillGainsLife() {
        SpinewoodsArmadillo libraryCard = new SpinewoodsArmadillo();
        harness.setHand(player1, List.of(new SpinewoodsArmadillo()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardingIsPaidBeforeTheAbilityResolves() {
        SpinewoodsArmadillo armadillo = new SpinewoodsArmadillo();
        harness.setHand(player1, List.of(armadillo));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(armadillo);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 23);
    }

    @Test
    void wardCountersOpposingRemovalWhenNoManaRemainsToPay() {
        Permanent armadillo = harness.addToBattlefieldAndReturn(player1, new SpinewoodsArmadillo());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ShootTheSheriff()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, armadillo.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(armadillo);
        harness.assertNotInGraveyard(player1, "Spinewoods Armadillo");
        harness.assertInGraveyard(player2, "Shoot the Sheriff");
    }
}
