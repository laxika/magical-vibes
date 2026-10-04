package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HairStrungKoto.class, HumbleBudoka.class})
class HairStrungKotoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped creature makes target player mill a card")
    void tapCreatureMillsTargetPlayer() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        harness.addToBattlefield(player1, new HumbleBudoka());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Humble Budoka").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller can be chosen as the milling player")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        harness.addToBattlefield(player1, new HumbleBudoka());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick creature can still pay the cost (no tap symbol)")
    void summoningSickCreaturePaysCost() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        bears.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature")
    void cannotActivateWithoutUntappedCreature() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only a creature controlled by the activator can pay the cost")
    void opponentCreatureCannotPayCost() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        Permanent opponentBears = addCreatureReady(player2, new HumbleBudoka());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller chooses which untapped creature pays the cost")
    void choosesCreatureToTapWhenMultipleAreAvailable() {
        Permanent koto = harness.addToBattlefieldAndReturn(player1, new HairStrungKoto());
        Permanent firstBears = addCreatureReady(player1, new HumbleBudoka());
        Permanent chosenBears = addCreatureReady(player1, new HumbleBudoka());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(koto),
                null, player2.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, chosenBears.getId());
        harness.passBothPriorities();

        assertThat(firstBears.isTapped()).isFalse();
        assertThat(chosenBears.isTapped()).isTrue();
        assertThat(koto.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The creature taps as a cost before the top card is milled on resolution")
    void paysCostBeforeMillingTopCard() {
        Permanent koto = harness.addToBattlefieldAndReturn(player1, new HairStrungKoto());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        HairStrungKoto topCard = new HairStrungKoto();
        HairStrungKoto nextCard = new HairStrungKoto();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(bears.isTapped()).isTrue();
        assertThat(koto.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("A tapped Koto can activate repeatedly using different untapped creatures")
    void tappedKotoCanActivateRepeatedly() {
        Permanent koto = harness.addToBattlefieldAndReturn(player1, new HairStrungKoto());
        Permanent firstBears = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        koto.tap();
        HairStrungKoto firstCard = new HairStrungKoto();
        HairStrungKoto secondCard = new HairStrungKoto();
        HairStrungKoto remainingCard = new HairStrungKoto();
        harness.setLibrary(player2, List.of(firstCard, secondCard, remainingCard));
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, firstBears.getId());
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(firstBears.isTapped()).isTrue();
        assertThat(secondBears.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(koto.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Milling an empty library pays the cost without making its owner lose")
    void emptyLibraryDoesNotCauseLoss() {
        harness.addToBattlefield(player1, new HairStrungKoto());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
