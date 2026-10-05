package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.ReadTheBones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MnemonicWall.class, Shock.class, GrizzlyBears.class, ReadTheBones.class})
class MnemonicWallTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns the chosen instant or sorcery card to hand")
    void returnsChosenInstantOrSorceryToHand() {
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock, new GrizzlyBears()));

        castMnemonicWall();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the optional return leaves the card in the graveyard")
    void decliningReturnsNothing() {
        harness.setGraveyard(player1, List.of(new Shock()));

        castMnemonicWall();

        Card shock = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("No instant or sorcery cards in the graveyard produce no choice")
    void noValidCardsProduceNoChoice() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castMnemonicWall();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mnemonic Wall");
    }

    @Test
    @DisplayName("ETB can return a sorcery from your graveyard")
    void returnsSorceryToHand() {
        Card sorcery = new ReadTheBones();
        harness.setGraveyard(player1, List.of(sorcery));

        castMnemonicWall();

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Read the Bones");
        harness.assertNotInGraveyard(player1, "Read the Bones");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only your graveyard supplies targets, even when an opponent has a sorcery")
    void excludesOpponentsGraveyard() {
        Card ownSorcery = new ReadTheBones();
        Card opposingSorcery = new ReadTheBones();
        harness.setGraveyard(player1, List.of(ownSorcery));
        harness.setGraveyard(player2, List.of(opposingSorcery));

        castMnemonicWall();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownSorcery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownSorcery.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Read the Bones");
        harness.assertInGraveyard(player2, "Read the Bones");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not returned or replaced by another card")
    void targetLeavesGraveyardBeforeResolution() {
        Card target = new ReadTheBones();
        Card otherSorcery = new ReadTheBones();
        harness.setGraveyard(player1, List.of(target, otherSorcery));

        castMnemonicWall();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherSorcery));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherSorcery);
        assertThat(gd.playerExiledCards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    private void castMnemonicWall() {
        harness.castFromHand(player1, new MnemonicWall(), "{4}{U}");
        harness.passBothPriorities();
    }
}
