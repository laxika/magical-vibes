package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VedalkenArchmage.class, AlphaMyr.class, Bonesplitter.class, LeoninSkyhunter.class})
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
        harness.forceActivePlayer(player2);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castFromHand(player2, new AlphaMyr(), "{2}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("The card is drawn before the artifact spell resolves")
    void drawsBeforeArtifactResolves() {
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.setLibrary(player1, List.of(new LeoninSkyhunter()));

        harness.castFromHand(player1, new AlphaMyr(), "{2}");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Alpha Myr");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Skyhunter");
        harness.assertNotOnBattlefield(player1, "Alpha Myr");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Alpha Myr");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A noncreature artifact spell also draws a card")
    void castingNoncreatureArtifactDrawsCard() {
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.setLibrary(player1, List.of(new LeoninSkyhunter()));

        harness.castFromHand(player1, new Bonesplitter(), "{1}");
        resolveAllTriggers();

        harness.assertInHand(player1, "Leonin Skyhunter");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("Each Archmage draws a card for the same artifact spell")
    void multipleArchmagesEachDrawCard() {
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.setLibrary(player1, List.of(new LeoninSkyhunter(), new AlphaMyr()));

        harness.castFromHand(player1, new Bonesplitter(), "{1}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Leonin Skyhunter");
        harness.assertInHand(player1, "Alpha Myr");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Bonesplitter");
    }

    @Test
    @DisplayName("An artifact entering without being cast does not draw a card")
    void artifactEnteringWithoutCastDoesNotDrawCard() {
        harness.addToBattlefield(player1, new VedalkenArchmage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LeoninSkyhunter()));

        harness.enterBattlefieldAndReturn(player1, new AlphaMyr());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Alpha Myr");
    }
}
