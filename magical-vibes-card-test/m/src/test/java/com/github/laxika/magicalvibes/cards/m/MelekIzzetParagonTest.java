package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.u.UncoveredClues;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MelekIzzetParagon.class, LightningBolt.class, GrizzlyBears.class, UncoveredClues.class})
class MelekIzzetParagonTest extends BaseCardTest {

    private long boltsOnStack() {
        return gd.stack.stream().filter(e -> e.getCard().getName().equals("Lightning Bolt")).count();
    }

    @Test
    @DisplayName("Casting an instant from the top of the library copies it")
    void libraryTopInstantIsCopied() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        gd.playerDecks.get(player1.getId()).addFirst(new LightningBolt());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1, player2.getId()); // resolve the copy trigger

        assertThat(boltsOnStack()).isEqualTo(2);
    }

    @Test
    @DisplayName("The copy resolves, so the spell's damage is dealt twice")
    void copyDealsDamageAgain() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        gd.playerDecks.get(player1.getId()).addFirst(new LightningBolt());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1, player2.getId()); // resolve the copy trigger
        harness.handleMayAbilityChosen(player1, false); // keep the copy's original target
        harness.passBothPriorities(); // resolve the copy
        harness.passBothPriorities(); // resolve the original

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Casting an instant from hand does not copy it")
    void handCastIsNotCopied() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(boltsOnStack()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature card on top of the library cannot be cast")
    void creatureOnTopIsNotCastable() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void libraryTopSorceryIsCopiedWithoutPayingItsCostAgain() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        harness.setLibrary(player1, List.of(new UncoveredClues()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry ->
                assertThat(entry.getCard().getName()).isEqualTo("Uncovered Clues"));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void copyCanChooseNewTargetWithoutChangingOriginal() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sorceryStillRequiresNormalTiming() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        UncoveredClues clues = new UncoveredClues();
        harness.setLibrary(player1, List.of(clues));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(clues);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void libraryCastingStillRequiresMana() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        LightningBolt bolt = new LightningBolt();
        harness.setLibrary(player1, List.of(bolt));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bolt);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void revealsTopCardToBothPlayersAndUpdatesAfterCasting() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        harness.setLibrary(player1, List.of(new LightningBolt(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Lightning Bolt\""));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Lightning Bolt\""));

        harness.castFromLibraryTop(player1, player2.getId());
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Grizzly Bears\""));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("\"name\":\"Grizzly Bears\""));
    }

    @Test
    void opponentDoesNotReceiveLibraryCastingPermission() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        LightningBolt bolt = new LightningBolt();
        harness.setLibrary(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bolt);
    }
}
