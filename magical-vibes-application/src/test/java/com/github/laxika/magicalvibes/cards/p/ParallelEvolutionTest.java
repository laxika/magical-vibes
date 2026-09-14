package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.s.SkywingAven;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParallelEvolution.class, BaskingRootwalla.class, SkywingAven.class, CabalCoffers.class})
class ParallelEvolutionTest extends BaseCardTest {

    private void addToken(Player player, Card card) {
        card.setToken(true);
        harness.addToBattlefield(player, card);
    }

    private List<Permanent> findTokens(Player player, String name) {
        return findPermanents(player, name).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private long countTokens(Player player, String name) {
        return findTokens(player, name).size();
    }

    @Test
    @DisplayName("Copies each creature token on every battlefield, but not nontokens or noncreature tokens")
    void copiesCreatureTokensOnEveryBattlefield() {
        addToken(player1, new BaskingRootwalla());
        addToken(player2, new SkywingAven());
        addToken(player1, new CabalCoffers());
        harness.addToBattlefield(player1, new BaskingRootwalla());

        harness.castFromHand(player1, new ParallelEvolution(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(countTokens(player1, "Basking Rootwalla")).isEqualTo(2);
        assertThat(findPermanents(player1, "Basking Rootwalla")).hasSize(3);
        assertThat(findTokens(player1, "Basking Rootwalla")).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
        assertThat(countTokens(player2, "Skywing Aven")).isEqualTo(2);
        assertThat(findTokens(player2, "Skywing Aven")).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        assertThat(countTokens(player1, "Cabal Coffers")).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback copies creature tokens and exiles Parallel Evolution")
    void flashbackCopiesCreatureTokensAndExilesSpell() {
        addToken(player1, new BaskingRootwalla());
        harness.setGraveyard(player1, List.of(new ParallelEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(countTokens(player1, "Basking Rootwalla")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Parallel Evolution");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Parallel Evolution"));
    }
}
