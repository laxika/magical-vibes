package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScourTheDesert.class, GrizzlyBears.class, HillGiant.class, Cancel.class})
class ScourTheDesertTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Exiles a creature from your graveyard and creates Birds equal to its toughness")
    void exilesCreatureAndCreatesBirdsEqualToToughness() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new ScourTheDesert()));
        giveMana();

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));

        List<?> birds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))
                .toList();
        assertThat(birds).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BIRD)))
                .allMatch(permanent -> permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getColor() == CardColor.WHITE
                        && permanent.getCard().getKeywords().contains(Keyword.FLYING));
    }

    @Test
    @DisplayName("Token count scales with the exiled creature's toughness")
    void tokenCountScalesWithToughness() {
        Card giant = new HillGiant();
        harness.setGraveyard(player1, List.of(giant));
        harness.setHand(player1, List.of(new ScourTheDesert()));
        giveMana();

        harness.castSorcery(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(giant.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BIRD)))
                .hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new ScourTheDesert()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void rejectsNonCreatureTarget() {
        Card cancel = new Cancel();
        harness.setGraveyard(player1, List.of(cancel));
        harness.setHand(player1, List.of(new ScourTheDesert()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, cancel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
