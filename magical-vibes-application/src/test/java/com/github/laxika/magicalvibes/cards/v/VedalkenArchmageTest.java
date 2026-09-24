package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VedalkenArchmage.class, AlphaMyr.class, LeoninSkyhunter.class})
class VedalkenArchmageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact spell draws a card")
    void castingArtifactDrawsCard() {
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.setHand(player1, List.of(new AlphaMyr(), new LeoninSkyhunter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Alpha Myr");
    }

    @Test
    @DisplayName("Casting a nonartifact spell does not draw a card")
    void castingNonartifactDoesNotDrawCard() {
        harness.addToBattlefield(player1, new VedalkenArchmage());

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player1, new LeoninSkyhunter(), "{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("An opponent casting an artifact spell does not trigger your Archmage")
    void opponentsArtifactDoesNotDrawCard() {
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.setHand(player2, List.of(new AlphaMyr()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }
}
