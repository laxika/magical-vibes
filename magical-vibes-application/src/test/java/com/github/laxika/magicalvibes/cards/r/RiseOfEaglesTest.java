package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiseOfEagles.class})
class RiseOfEaglesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rise of Eagles creates two blue Bird enchantment creature tokens")
    void createsBirdTokensAndStartsScry() {
        harness.setHand(player1, List.of(new RiseOfEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> birds = findPermanents(player1, "Bird");
        assertThat(birds).hasSize(2);
        for (Permanent bird : birds) {
            assertThat(bird.getCard().getPower()).isEqualTo(2);
            assertThat(bird.getCard().getToughness()).isEqualTo(2);
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(bird.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Rise of Eagles scries one card after creating the tokens")
    void scryCanPutCardOnBottom() {
        harness.setHand(player1, List.of(new RiseOfEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        var originalTop = gd.playerDecks.get(player1.getId()).getFirst();
        harness.castAndResolveSorcery(player1, 0, 0);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rise of Eagles");
    }

    @Test
    @DisplayName("Rise of Eagles can keep the scryed card on top without changing library order")
    void scryCanKeepCardOnTop() {
        RiseOfEagles top = new RiseOfEagles();
        RiseOfEagles bottom = new RiseOfEagles();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setHand(player1, List.of(new RiseOfEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rise of Eagles");
    }

    @Test
    @DisplayName("Rise of Eagles still creates its tokens with an empty library")
    void createsTokensWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RiseOfEagles()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rise of Eagles");
    }

}
