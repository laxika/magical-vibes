package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.DeckDefinition;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.DeckValidationService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CantQuiteRecall.class, ColossalDreadmaw.class})
class CantQuiteRecallTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws three cards")
    void targetPlayerDrawsThreeCards() {
        harness.setHand(player1, List.of(new CantQuiteRecall()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new ColossalDreadmaw(), new ColossalDreadmaw(), new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new CantQuiteRecall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Colossal Dreadmaw")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Caster can draw exactly three cards, leaving the fourth in the library")
    void casterDrawsExactlyThreeCards() {
        CantQuiteRecall spell = new CantQuiteRecall();
        List<ColossalDreadmaw> library = List.of(new ColossalDreadmaw(), new ColossalDreadmaw(),
                new ColossalDreadmaw(), new ColossalDreadmaw());
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, library);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library.subList(0, 3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Target loses when its library contains only two cards")
    void targetLosesWhenUnableToDrawThirdCard() {
        List<ColossalDreadmaw> library = List.of(new ColossalDreadmaw(), new ColossalDreadmaw());
        harness.setHand(player1, List.of(new CantQuiteRecall()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Forbidden prevents inclusion in a casual starting deck")
    void forbiddenCardCannotBeInStartingDeck() {
        DeckDefinition deck = new DeckDefinition(List.of(
                new CantQuiteRecall(), new CantQuiteRecall(), new CantQuiteRecall(),
                new ColossalDreadmaw(), new ColossalDreadmaw(), new ColossalDreadmaw(), new ColossalDreadmaw()),
                List.of(), null);
        DeckValidationService validationService = new DeckValidationService(null);

        assertThat(validationService.validate(deck, DeckFormat.CASUAL).valid()).isFalse();
    }
}
