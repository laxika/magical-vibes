package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TolarianTerror;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkywriterDjinn.class, Plains.class, Island.class, Forest.class, Swamp.class,
        Mountain.class, TolarianTerror.class})
class SkywriterDjinnTest extends BaseCardTest {

    private static final Set<String> SPELLBOOK = Set.of(
            "See the Truth", "Teferi's Time Twist", "Flood of Recollection", "Keep Safe",
            "Hard Evidence", "Ghostform", "Startle", "Hampering Snare", "Stifle",
            "Contentious Plan", "Majestic Metamorphosis", "Befuddle", "Bury in Books",
            "Choking Tethers", "Suit Up");

    @Test
    @DisplayName("ETB conjures one spellbook card without Domain")
    void etbConjuresOneCard() {
        harness.setHand(player1, List.of(new SkywriterDjinn()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .hasSize(1)
                .allMatch(Card::isTokenCard)
                .extracting(Card::getName)
                .allMatch(SPELLBOOK::contains);
    }

    @Test
    @DisplayName("Domain repeats until hand size reaches the number of basic land types")
    void domainRepeatsForBasicLandTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SkywriterDjinn()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .hasSize(3)
                .allMatch(Card::isTokenCard)
                .extracting(Card::getName)
                .allMatch(SPELLBOOK::contains);
    }

    @Test
    @DisplayName("Full Domain conjures five cards from an empty hand")
    void conjuresFiveCardsWithFullDomain() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        harness.enterBattlefieldAndReturn(player1, new SkywriterDjinn());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5)
                .extracting(Card::getName).allMatch(SPELLBOOK::contains);
    }

    @Test
    @DisplayName("Always conjures once even when the hand already exceeds Domain")
    void conjuresWithHandAboveDomain() {
        harness.addToBattlefield(player1, new Island());
        Card retained = new Plains();
        harness.setHand(player1, List.of(new SkywriterDjinn(), retained, new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(retained);
        assertThat(gd.playerHands.get(player1.getId()).getLast().getName()).isIn(SPELLBOOK);
    }

    @Test
    @DisplayName("Duplicate land types and opponents' lands do not increase Domain")
    void countsOnlyDistinctControlledLandTypes() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Forest());

        harness.enterBattlefieldAndReturn(player1, new SkywriterDjinn());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Domain and hand size are checked when the trigger resolves")
    void checksDomainAndHandAtResolution() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new Plains());
        harness.enterBattlefieldAndReturn(player1, new SkywriterDjinn());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        Card retained = new Island();
        harness.setHand(player1, List.of(retained));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(retained);
        assertThat(gd.playerHands.get(player1.getId()).subList(1, 3))
                .extracting(Card::getName).allMatch(SPELLBOOK::contains);
    }

    @Test
    @DisplayName("Conjuring works with an empty library and gives cards to the trigger controller")
    void conjuresForOtherControllerWithoutDrawing() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());

        harness.enterBattlefieldAndReturn(player2, new SkywriterDjinn());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2)
                .allMatch(card -> player2.getId().equals(card.getOwnerId()))
                .extracting(Card::getName).allMatch(SPELLBOOK::contains);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("A conjured spell in the graveyard counts toward Tolarian Terror's cost reduction")
    void conjuredSpellCountsAsCardInGraveyard() {
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new SkywriterDjinn());
        resolveAllTriggers();
        Card conjured = gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new TolarianTerror()));
        harness.setGraveyard(player1, List.of(conjured));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tolarian Terror");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(conjured);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
