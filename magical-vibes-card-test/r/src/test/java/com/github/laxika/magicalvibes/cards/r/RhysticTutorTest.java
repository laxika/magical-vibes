package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.p.PlagueFiend;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RhysticTutor.class, RebelInformer.class, PlagueFiend.class, Swamp.class})
class RhysticTutorTest extends BaseCardTest {

    @Test
    void searchesLibraryWhenNoPlayerPays() {
        harness.setLibrary(player1, List.of(new RebelInformer(), new PlagueFiend()));
        castTutor();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof RebelInformer);
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(1)
                .allMatch(card -> card instanceof PlagueFiend);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anyPlayerCanPayToPreventTheSearch() {
        harness.setLibrary(player1, List.of(new RebelInformer()));
        castTutor();

        harness.handleMayAbilityChosen(player1, false);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof RebelInformer);
    }

    @Test
    void controllerCanPayToPreventTheSearch() {
        harness.setLibrary(player1, List.of(new RebelInformer()));
        castTutor();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof RebelInformer);
    }

    @Test
    void searchesForANoncreatureCardAndCannotFailToFind() {
        RhysticTutor libraryCard = new RhysticTutor();
        harness.setLibrary(player1, List.of(libraryCard));
        castTutor();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Rhystic Tutor");
    }

    @Test
    void resolvesWithAnEmptyLibraryWhenNobodyPays() {
        harness.setLibrary(player1, List.of());
        castTutor();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Rhystic Tutor");
    }

    @Test
    void opponentCanPayGenericCostWithColoredMana() {
        RebelInformer libraryCard = new RebelInformer();
        harness.setLibrary(player1, List.of(libraryCard));
        castTutor();

        harness.handleMayAbilityChosen(player1, false);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Rhystic Tutor");
    }
    @Test
    @DisplayName("After the controller pays, the other player is still offered the payment (PCY ruling)")
    void otherPlayersStillGetTheOptionAfterAPayment() {
        harness.setLibrary(player1, List.of(new RebelInformer()));
        castTutor();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull()
                .extracting(PendingInteraction.MayAbilityChoice::playerId)
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof RebelInformer);
    }

    @Test
    @DisplayName("A payment by the controller still prevents the search when the other player declines")
    void earlierPaymentCountsWhenLaterPlayerDeclines() {
        harness.setLibrary(player1, List.of(new RebelInformer()));
        castTutor();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof RebelInformer);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A player with no mana source and an empty pool isn't asked; the others still are")
    void playerWithNoWayToPayIsNotAsked() {
        harness.setLibrary(player1, List.of(new RebelInformer()));
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.setSummoningSick(false);
        harness.castFromHand(player1, new RhysticTutor(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    private void castTutor() {
        giveManaSource(player1);
        giveManaSource(player2);
        harness.castFromHand(player1, new RhysticTutor(), "{2}{B}");
        harness.passBothPriorities();
    }
}
