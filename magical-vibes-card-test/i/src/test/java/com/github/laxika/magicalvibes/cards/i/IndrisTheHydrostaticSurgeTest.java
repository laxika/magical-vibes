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

        harness.castAndResolveInstant(player1, 0, player2.getId());

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
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(Card::getName, "Grizzly Bears")
                .hasSize(3);
    }

    @Test
    void instantCastDrawsBeforeTheSpellResolves() {
        addCreatureReady(player1, new IndrisTheHydrostaticSurge());
        Card drawnCard = new LightningBolt();
        harness.setLibrary(player1, List.of(drawnCard, new LightningBolt()));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertLife(player2, 17);
    }

    @Test
    void opponentsInstantDoesNotDrawACard() {
        addCreatureReady(player1, new IndrisTheHydrostaticSurge());
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 17);
    }

    @Test
    void creatureCastDoesNotDrawACard() {
        addCreatureReady(player1, new IndrisTheHydrostaticSurge());
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void enteringUnderOpponentControlConjuresIntoTheirEmptyLibrary() {
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.setLibrary(player2, List.of());
        harness.enterBattlefieldAndReturn(player2, new IndrisTheHydrostaticSurge());

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(4)
                .allSatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Lightning Bolt");
                    assertThat(card.getOwnerId()).isEqualTo(player2.getId());
                });
    }

    @Test
    void stormCountsOpponentsSpellAndCopiesDoNotTriggerAdditionalDraws() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new IndrisTheHydrostaticSurge());
        resolveAllTriggers();
        Card conjuredBolt = gd.playerDecks.get(player1.getId()).removeFirst();
        harness.setHand(player1, List.of(conjuredBolt));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(conjuredBolt);
    }

    @Test
    void conjuredBoltsKeepStormAfterIndrisDies() {
        harness.setLibrary(player1, List.of());
        var indris = harness.enterBattlefieldAndReturn(player1, new IndrisTheHydrostaticSurge());
        resolveAllTriggers();
        Card conjuredBolt = gd.playerDecks.get(player1.getId()).removeFirst();
        harness.setHand(player1, List.of(conjuredBolt));
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, indris.getId());
        harness.castAndResolveInstant(player2, 0, indris.getId());
        harness.assertNotOnBattlefield(player1, "Indris, the Hydrostatic Surge");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 11);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
