package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrostaniSelesnyasVoiceTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering causes life gain equal to its toughness")
    void anotherCreatureEnteringGainsLifeEqualToToughness() {
        harness.addToBattlefield(player1, new TrostaniSelesnyasVoice());
        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Trostani does not trigger for its own entry")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new TrostaniSelesnyasVoice()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Populate creates a copy of a creature token and triggers life gain")
    void populateCreatesTokenCopyAndGainsLife() {
        addCreatureReady(player1, new TrostaniSelesnyasVoice());
        harness.addToBattlefield(player1, creatureToken("Rhino Token", 4, 4));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rhino Token")).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    private static Card creatureToken(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
