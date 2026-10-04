package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SatyrWayfinder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FatedIntervention.class, SatyrWayfinder.class})
class FatedInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 3/3 green Centaur enchantment creature tokens")
    void createsCentaurEnchantmentCreatureTokens() {
        cast(player1);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId());
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(3);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CENTAUR);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        });
    }

    @Test
    @DisplayName("Scries 2 when cast during your turn")
    void scriesOnYourTurn() {
        harness.setLibrary(player1, List.of(new SatyrWayfinder(), new SatyrWayfinder(), new SatyrWayfinder()));

        cast(player1);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Does not scry when cast during an opponent's turn")
    void doesNotScryOnOpponentsTurn() {
        SatyrWayfinder first = new SatyrWayfinder();
        SatyrWayfinder second = new SatyrWayfinder();
        harness.setLibrary(player1, List.of(first, second));

        cast(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    private void cast(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FatedIntervention(), "{2}{G}{G}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creates tokens before scrying and permits putting either revealed card on the bottom")
    void createsTokensBeforeScryAndOrdersLibrary() {
        SatyrWayfinder first = new SatyrWayfinder();
        SatyrWayfinder second = new SatyrWayfinder();
        SatyrWayfinder third = new SatyrWayfinder();
        harness.setLibrary(player1, List.of(first, second, third));

        cast(player1);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof FatedIntervention);
    }

    @Test
    @DisplayName("Scry 2 works with only one card in the library")
    void scriesWithOneCardInLibrary() {
        SatyrWayfinder onlyCard = new SatyrWayfinder();
        harness.setLibrary(player1, List.of(onlyCard));

        cast(player1);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
