package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glimmerburst.class})
class GlimmerburstTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and creates a 1/1 white Glimmer enchantment creature token")
    void drawsTwoCardsAndCreatesGlimmerToken() {
        harness.setLibrary(player1, List.of(new Glimmerburst(), new Glimmerburst()));
        harness.castFromHand(player1, new Glimmerburst(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Glimmerburst", "Glimmerburst");

        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(glimmer);
        assertThat(glimmer.getCard().isToken()).isTrue();
        assertThat(glimmer.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(glimmer.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(glimmer.getCard().getSubtypes()).containsExactly(CardSubtype.GLIMMER);
        assertThat(glimmer.getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(glimmer.getCard().getPower()).isEqualTo(1);
        assertThat(glimmer.getCard().getToughness()).isEqualTo(1);
        assertThat(glimmer.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Glimmerburst");
    }

    @Test
    @DisplayName("On an opponent's turn, only the caster draws and receives the token")
    void resolvesForCasterOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of());
        Glimmerburst first = new Glimmerburst();
        Glimmerburst second = new Glimmerburst();
        Glimmerburst third = new Glimmerburst();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(new Glimmerburst()));

        harness.castFromHand(player1, new Glimmerburst(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Glimmer");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
