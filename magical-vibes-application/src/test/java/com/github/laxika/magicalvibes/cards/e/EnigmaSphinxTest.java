package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnigmaSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("When Enigma Sphinx dies it is tucked into its owner's library third from the top")
    void diesGoesThirdFromTop() {
        harness.addToBattlefield(player1, new EnigmaSphinx());
        Permanent sphinx = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card sphinxCard = sphinx.getCard();

        Card top = new Plains();
        Card second = new Mountain();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(top, second, third));

        killWithWrath();

        // Inserted at index 2 (third from the top): [top, second, sphinx, third].
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.get(2).getId()).isEqualTo(sphinxCard.getId());
        assertThat(library).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId(), sphinxCard.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(sphinxCard.getId()));
    }

    @Test
    @DisplayName("With fewer cards than the target position, Enigma Sphinx is placed on the bottom of the library")
    void tooFewCardsGoesToBottom() {
        harness.addToBattlefield(player1, new EnigmaSphinx());
        Permanent sphinx = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card sphinxCard = sphinx.getCard();

        Card only = new Plains();
        harness.setLibrary(player1, List.of(only));

        killWithWrath();

        // Position 2 clamps to the bottom of a 1-card library: [only, sphinx].
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).extracting(Card::getId).containsExactly(only.getId(), sphinxCard.getId());
    }

    private void killWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — the Sphinx dies, death trigger goes on the stack
        harness.passBothPriorities(); // resolve the death trigger — tuck into library
    }
}
