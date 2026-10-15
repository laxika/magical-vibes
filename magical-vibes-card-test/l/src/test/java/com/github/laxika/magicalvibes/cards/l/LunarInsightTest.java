package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DelverOfSecrets;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InsectileAberration;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.o.Omniscience;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LunarInsight.class, GrizzlyBears.class, Island.class, Memnite.class,
        Omniscience.class, SerraAngel.class})
class LunarInsightTest extends BaseCardTest {

    private void castLunarInsight() {
        harness.castFromHand(player1, new LunarInsight(), "{2}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws one card for each distinct mana value among controlled nonland permanents")
    void drawsForDistinctManaValues() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new Island());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castLunarInsight();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Counts only nonland permanents controlled by the caster")
    void ignoresLandsAndOpponentsPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new SerraAngel());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castLunarInsight();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws no cards with no permanents")
    void drawsNothingWithEmptyBattlefield() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castLunarInsight();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands alone do not contribute mana value zero")
    void drawsNothingWithOnlyLands() {
        harness.addToBattlefield(player1, new Island());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castLunarInsight();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counts noncreature permanents entering before resolution")
    void countsNoncreaturePermanentAtResolution() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.castFromHand(player1, new LunarInsight(), "{2}{U}");
        harness.addToBattlefield(player1, new Omniscience());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Face-down permanents contribute zero rather than their printed mana value")
    void countsFaceDownPermanentAsZero() {
        var faceDown = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefield(player1, new SerraAngel());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castLunarInsight();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({DelverOfSecrets.class, InsectileAberration.class})
    @DisplayName("Transformed permanents retain their front face mana value")
    void countsFrontFaceManaValueOfTransformedPermanent() {
        var delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());
        delver.setCard(delver.getOriginalCard().getBackFaceCard());
        delver.setTransformed(true);
        harness.addToBattlefield(player1, new Memnite());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castLunarInsight();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
