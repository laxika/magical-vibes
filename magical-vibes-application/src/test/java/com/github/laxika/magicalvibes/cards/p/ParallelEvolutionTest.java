package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ParallelEvolution.class)
class ParallelEvolutionTest extends BaseCardTest {

    private void addToken(Player player, String name, CardType type) {
        Card card = new Card();
        card.setToken(true);
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private long countTokens(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }

    @Test
    @DisplayName("Copies creature tokens on every battlefield, but not noncreature tokens")
    void copiesCreatureTokensOnEveryBattlefield() {
        addToken(player1, "Saproling", CardType.CREATURE);
        addToken(player2, "Saproling", CardType.CREATURE);
        addToken(player1, "Treasure", CardType.ARTIFACT);
        harness.setHand(player1, List.of(new ParallelEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countTokens(player1, "Saproling")).isEqualTo(2);
        assertThat(countTokens(player2, "Saproling")).isEqualTo(2);
        assertThat(countTokens(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback copies creature tokens and exiles Parallel Evolution")
    void flashbackCopiesCreatureTokensAndExilesSpell() {
        addToken(player1, "Saproling", CardType.CREATURE);
        harness.setGraveyard(player1, List.of(new ParallelEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(countTokens(player1, "Saproling")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Parallel Evolution");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Parallel Evolution"));
    }
}
