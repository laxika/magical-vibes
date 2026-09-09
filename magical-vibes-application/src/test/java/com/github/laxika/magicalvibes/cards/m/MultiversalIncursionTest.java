package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MultiversalIncursion.class, GrizzlyBears.class})
class MultiversalIncursionTest extends BaseCardTest {

    @Test
    void copiesEachNontokenCreatureAndSkipsCreatureTokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        addCreatureToken(player1, "Token Bear");
        castMultiversalIncursion();

        assertThat(tokensNamed(player1, "Grizzly Bears")).hasSize(1);
        assertThat(tokensNamed(player1, "Token Bear")).hasSize(1);
    }

    @Test
    void copiedLegendaryCreatureIsNotLegendary() {
        addLegendaryCreature(player1);
        castMultiversalIncursion();

        List<Permanent> copies = tokensNamed(player1, "Legendary Bear");
        assertThat(copies).hasSize(1);
        assertThat(copies.get(0).getCard().getSupertypes())
                .doesNotContain(CardSupertype.LEGENDARY);
    }

    private void castMultiversalIncursion() {
        harness.setHand(player1, List.of(new MultiversalIncursion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }

    private Permanent addCreatureToken(Player player, String name) {
        Card card = new Card();
        card.setToken(true);
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addLegendaryCreature(Player player) {
        Card card = new Card();
        card.setName("Legendary Bear");
        card.setType(CardType.CREATURE);
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private List<Permanent> tokensNamed(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .toList();
    }
}
