package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.EldraziDevastator;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OracleOfDust.class, EldraziDevastator.class, ScourFromExistence.class})
class OracleOfDustTest extends BaseCardTest {

    @Test
    void processesAnOpponentOwnedExiledCardThenDrawsAndDiscards() {
        Permanent oracle = addReadyOracle();
        EldraziDevastator heldCard = new EldraziDevastator();
        OracleOfDust drawnCard = new OracleOfDust();
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setHand(player1, List.of(heldCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(oracle.isTapped()).isFalse();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        harness.assertInGraveyard(player2, "Scour from Existence");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(heldCard.getId(), drawnCard.getId());

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Eldrazi Devastator");
        harness.assertInHand(player1, "Oracle of Dust");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void promptsToChooseAmongMultipleOpponentOwnedExiledCards() {
        addReadyOracle();
        ScourFromExistence first = new ScourFromExistence();
        ScourFromExistence second = new ScourFromExistence();
        harness.setExile(player2, List.of(first, second));
        harness.setHand(player1, List.of(new EldraziDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        harness.assertInGraveyard(player2, "Scour from Existence");
    }

    @Test
    void cannotActivateWithoutAnOpponentOwnedExiledCard() {
        addReadyOracle();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private Permanent addReadyOracle() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new OracleOfDust());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void processingChoiceDoesNotRevealOpponentFaceDownExiledCards() {
        addReadyOracle();
        gd.addToExile(player2.getId(), new ScourFromExistence(), null, true, player2.getId());
        gd.addToExile(player2.getId(), new EldraziDevastator(), null, true, player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, null);

        List<String> prompts = harness.getConn1().getMessagesContaining("INTERACTION_PROMPT");
        assertThat(prompts).isNotEmpty();
        assertThat(prompts).allSatisfy(prompt -> assertThat(prompt)
                .doesNotContain("Scour from Existence", "Eldrazi Devastator"));
    }

    @Test
    void cannotProcessItsControllersOwnExiledCard() {
        addReadyOracle();
        ScourFromExistence ownCard = new ScourFromExistence();
        harness.setExile(player1, List.of(ownCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThat(gd.findExiledCard(ownCard.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotProcessTheExiledCard() {
        addReadyOracle();
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndDiscardTheDrawnCard() {
        Permanent oracle = harness.addToBattlefieldAndReturn(player1, new OracleOfDust());
        oracle.setSummoningSick(true);
        oracle.tap();
        EldraziDevastator drawnCard = new EldraziDevastator();
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard, new OracleOfDust()));
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Eldrazi Devastator");
        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(oracle.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

}
