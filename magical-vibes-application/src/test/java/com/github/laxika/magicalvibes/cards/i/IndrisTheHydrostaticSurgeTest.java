package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndrisTheHydrostaticSurge.class, Divination.class, GrizzlyBears.class, LightningBolt.class})
class IndrisTheHydrostaticSurgeTest extends BaseCardTest {

    @Test
    void entersAndConjuresFourStormLightningBoltsIntoLibrary() {
        harness.setLibrary(player1, cards(10));
        harness.enterBattlefieldAndReturn(player1, new IndrisTheHydrostaticSurge());
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(14);
        assertThat(library).filteredOn(Card::getName, "Lightning Bolt").hasSize(4);
        assertThat(library.stream()
                .filter(card -> "Lightning Bolt".equals(card.getName()))
                .allMatch(card -> player1.getId().equals(card.getOwnerId())))
                .isTrue();
    }

    @Test
    void conjuredLightningBoltHasStorm() {
        harness.setLibrary(player1, cards(5));
        harness.enterBattlefieldAndReturn(player1, new IndrisTheHydrostaticSurge());
        resolveAllTriggers();

        Card conjuredBolt = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> "Lightning Bolt".equals(card.getName()))
                .findFirst()
                .orElseThrow();
        gd.playerDecks.get(player1.getId()).remove(conjuredBolt);
        harness.setHand(player1, List.of(conjuredBolt));
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    void drawsWhenYouCastAnInstantOrSorcery() {
        addCreatureReady(player1, new IndrisTheHydrostaticSurge());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(Card::getName, "Grizzly Bears")
                .hasSize(3);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
