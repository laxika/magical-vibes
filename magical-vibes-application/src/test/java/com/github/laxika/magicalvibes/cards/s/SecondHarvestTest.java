package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecondHarvestTest extends BaseCardTest {

    private List<Permanent> tokensNamed(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(name))
                .toList();
    }

    private Permanent addToken(Player player, String name, CardType type, List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setToken(true);
        card.setName(name);
        card.setType(type);
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }
        card.setSubtypes(subtypes);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Copies each creature token you control; nontokens are ignored")
    void copiesEachCreatureToken() {
        addToken(player1, "Elf Warrior", CardType.CREATURE, List.of(CardSubtype.ELF, CardSubtype.WARRIOR));
        addToken(player1, "Elf Warrior", CardType.CREATURE, List.of(CardSubtype.ELF, CardSubtype.WARRIOR));
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(tokensNamed(player1, "Elf Warrior")).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Also copies noncreature tokens you control")
    void copiesNoncreatureTokens() {
        addToken(player1, "Treasure", CardType.ARTIFACT, List.of(CardSubtype.TREASURE));
        addToken(player1, "Saproling", CardType.CREATURE, List.of(CardSubtype.SAPROLING));
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(tokensNamed(player1, "Treasure")).hasSize(2);
        assertThat(tokensNamed(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("With no tokens, creates nothing")
    void withNoTokensCreatesNothing() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int before = gd.playerBattlefields.get(player1.getId()).size();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before);
    }
}
