package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkstarBanisher.class, GrizzlyBears.class, Forest.class, SerraAngel.class,
        LightningBolt.class, HillGiant.class, SolRing.class, Panharmonicon.class})
class DarkstarBanisherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets only an opposing nonland permanent with mana value 4 or less")
    void etbExilesMatchingPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castBanisher();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bears.getId(), giant.getId())
                .doesNotContain(angel.getId(), forest.getId(), ownBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("When it leaves, the exiled card's owner seeks a card sharing its card type")
    void leavesCausesExiledCardOwnerToSeek() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new SerraAngel()));

        castBanisher();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        destroyBanisher();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Serra Angel");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Exiling an artifact causes its owner to seek an artifact rather than a creature")
    void seeksMatchingArtifact() {
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears(), new SolRing()));

        castBanisher();
        harness.handlePermanentChosen(player1, ring.getId());
        harness.passBothPriorities();

        destroyBanisher();
        resolveAllTriggers();

        harness.assertInHand(player2, "Sol Ring");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Sol Ring");
    }

    @Test
    @DisplayName("Seek does nothing when the owner's library has no matching card type")
    void noMatchingCardDoesNotChangeLibrary() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Forest(), new LightningBolt()));
        harness.setHand(player2, List.of());

        castBanisher();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        destroyBanisher();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Lightning Bolt");
    }

    @Test
    @DisplayName("Seek does nothing when the owner's library is empty")
    void emptyLibraryDoesNotDrawOrLoseGame() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of());

        castBanisher();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        destroyBanisher();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Leaving before the enter trigger resolves still exiles the target without seeking")
    void leavesBeforeExilingDoesNotSeek() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new SerraAngel()));
        harness.setHand(player2, List.of());

        castBanisher();
        harness.handlePermanentChosen(player1, bears.getId());
        destroyBanisher();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Serra Angel");
    }

    @Test
    @DisplayName("Seek considers the types of all cards exiled by doubled enter triggers")
    void seekConsidersEveryLinkedExiledCard() {
        harness.addToBattlefield(player1, new Panharmonicon());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new SolRing()));
        harness.setHand(player2, List.of());

        castBanisher();
        harness.handlePermanentChosen(player1, ring.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Sol Ring");

        destroyBanisher();
        resolveAllTriggers();

        harness.assertInHand(player2, "Sol Ring");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void destroyBanisher() {
        UUID banisherId = harness.getPermanentId(player1, "Darkstar Banisher");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, banisherId);
    }

    private void castBanisher() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DarkstarBanisher(), "{1}{W}{B}");
        harness.passBothPriorities();
    }
}
