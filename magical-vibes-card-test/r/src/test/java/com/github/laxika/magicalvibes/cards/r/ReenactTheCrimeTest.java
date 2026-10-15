package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DemandAnswers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HaakonStromgaldScourge;
import com.github.laxika.magicalvibes.cards.p.PickYourPoison;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReenactTheCrime.class, Forest.class, GrizzlyBears.class, TormentingVoice.class,
        DemandAnswers.class, RedHerring.class, PickYourPoison.class, HaakonStromgaldScourge.class})
class ReenactTheCrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Only a nonland card put into a graveyard this turn can be targeted")
    void onlyCardPutIntoGraveyardThisTurnCanBeTargeted() {
        GrizzlyBears oldGraveyardCard = new GrizzlyBears();
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldGraveyardCard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TormentingVoice(), discardedCard, new ReenactTheCrime()));
        addManaForVoiceAndReenact();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, oldGraveyardCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.castAndResolveInstant(player1, 0, discardedCard.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exiles the card and offers a free copy cast")
    void exilesCardAndCastsCopyForFree() {
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TormentingVoice(), discardedCard, new ReenactTheCrime()));
        addManaForVoiceAndReenact();

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, discardedCard.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(discardedCard.getId()));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void decliningCopyStillExilesOriginal() {
        RedHerring discardedCard = new RedHerring();
        discardWithDemandAnswers(player1, discardedCard);
        harness.setHand(player1, List.of(new ReenactTheCrime()));
        addManaForReenact();

        harness.castAndResolveInstant(player1, 0, discardedCard.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(discardedCard.getId());
        harness.assertNotInGraveyard(player1, "Red Herring");
        harness.assertNotOnBattlefield(player1, "Red Herring");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castsOpponentsCardUnderYourControlAsAToken() {
        RedHerring discardedCard = new RedHerring();
        discardWithDemandAnswers(player2, discardedCard);
        harness.setHand(player1, List.of(new ReenactTheCrime()));
        addManaForReenact();

        harness.castAndResolveInstant(player1, 0, discardedCard.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).containsExactly(discardedCard.getId());
        harness.assertNotInGraveyard(player2, "Red Herring");
        harness.assertNotOnBattlefield(player2, "Red Herring");
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isNotEqualTo(discardedCard.getId());
            assertThat(permanent.getCard().isToken()).isTrue();
            assertThat(permanent.isCast()).isTrue();
            assertThat(permanent.getCastControllerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    void cannotTargetLandDiscardedThisTurn() {
        Forest discardedLand = new Forest();
        discardWithDemandAnswers(player1, discardedLand);
        harness.setHand(player1, List.of(new ReenactTheCrime()));
        addManaForReenact();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, discardedLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fizzlesWhenAnotherReenactExilesItsTargetFirst() {
        RedHerring discardedCard = new RedHerring();
        discardWithDemandAnswers(player1, discardedCard);
        harness.setHand(player1, List.of(new ReenactTheCrime()));
        harness.setHand(player2, List.of(new ReenactTheCrime()));
        addManaForReenact();
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, discardedCard.getId());
        harness.castInstant(player2, 0, discardedCard.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(discardedCard.getId());
        harness.assertNotOnBattlefield(player1, "Red Herring");
        harness.assertNotOnBattlefield(player2, "Red Herring");
    }

    @Test
    void allowsCastingCopyWithPayableMandatoryAdditionalCost() {
        DemandAnswers discardedCard = new DemandAnswers();
        discardWithDemandAnswers(player1, discardedCard);
        harness.setHand(player1, List.of(new ReenactTheCrime()));
        harness.addToBattlefield(player1, new RedHerring());
        addManaForReenact();

        harness.castAndResolveInstant(player1, 0, discardedCard.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack.isEmpty() && !gd.interaction.isAwaitingInput())
                .as("The legal copy cast must either request its additional cost or reach the stack")
                .isFalse();
    }

    @Test
    void offersModeChoiceWhenCastingAModalCopy() {
        PickYourPoison discardedCard = new PickYourPoison();
        discardWithDemandAnswers(player1, discardedCard);
        harness.setHand(player1, List.of(new ReenactTheCrime()));
        addManaForReenact();

        harness.castAndResolveInstant(player1, 0, discardedCard.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput())
                .as("Casting the copy requires the controller to choose a mode")
                .isTrue();
    }

    @Test
    void cannotCastCopyOfACardRestrictedToCastingFromGraveyard() {
        HaakonStromgaldScourge discardedCard = new HaakonStromgaldScourge();
        discardWithDemandAnswers(player1, discardedCard);
        harness.setHand(player1, List.of(new ReenactTheCrime()));
        addManaForReenact();

        harness.castAndResolveInstant(player1, 0, discardedCard.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(discardedCard.getId());
        harness.assertNotOnBattlefield(player1, "Haakon, Stromgald Scourge");
    }

    private void discardWithDemandAnswers(Player player, Card discardedCard) {
        harness.setLibrary(player, List.of(new Forest(), new Forest()));
        harness.setHand(player, List.of(new DemandAnswers(), discardedCard));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstantWithDiscard(player, 0, null, 1);
        harness.passBothPriorities();
    }

    private void addManaForReenact() {
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addManaForVoiceAndReenact() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
