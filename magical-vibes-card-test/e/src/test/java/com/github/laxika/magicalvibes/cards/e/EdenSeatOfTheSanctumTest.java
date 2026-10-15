package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TravelingChocobo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdenSeatOfTheSanctum.class, Forest.class, TravelingChocobo.class})
class EdenSeatOfTheSanctumTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent eden = addReadyEden();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(eden.isTapped()).isTrue();
    }

    @Test
    void millsThenMaySacrificeAndReturnAnotherPermanentCard() {
        Permanent eden = addReadyEden();
        Card returned = new TravelingChocobo();
        harness.setGraveyard(player1, List.of(returned));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, eden.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(eden.getCard().getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Traveling Chocobo");
        harness.assertNotOnBattlefield(player1, "Eden, Seat of the Sanctum");
        harness.assertInGraveyard(player1, "Eden, Seat of the Sanctum");
    }

    @Test
    void decliningSacrificeStillMillsAndKeepsEden() {
        Permanent eden = addReadyEden();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(eden.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Eden, Seat of the Sanctum");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetFreshlyMilledPermanentAndReturnWaitsForTriggerResolution() {
        Permanent eden = addReadyEden();
        Card returned = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(returned));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, eden.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Eden, Seat of the Sanctum");
    }

    @Test
    void cannotReturnCardFromOpponentsGraveyard() {
        Permanent eden = addReadyEden();
        Card ownCard = new Forest();
        Card opponentCard = new Forest();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, eden.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCard).doesNotContain(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
    }

    @Test
    void canSacrificeWithEmptyLibraryAndNoOtherPermanentToReturn() {
        Permanent eden = addReadyEden();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, eden.getId());

        harness.assertNotOnBattlefield(player1, "Eden, Seat of the Sanctum");
        harness.assertInGraveyard(player1, "Eden, Seat of the Sanctum");
        harness.assertNotInHand(player1, "Eden, Seat of the Sanctum");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyEden() {
        Permanent eden = harness.addToBattlefieldAndReturn(player1, new EdenSeatOfTheSanctum());
        eden.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return eden;
    }
}
