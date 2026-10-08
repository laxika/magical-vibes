package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxOfJwarIsle.class, WelkinTern.class, KrakenHatchling.class, IntoTheRoil.class})
class SphinxOfJwarIsleTest extends BaseCardTest {

    @Test
    void onlyControllerSeesOwnLibraryTopCard() {
        harness.addToBattlefield(player1, new SphinxOfJwarIsle());
        harness.setLibrary(player1, List.of(new WelkinTern()));
        harness.setLibrary(player2, List.of(new KrakenHatchling()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Welkin Tern"))
                .noneMatch(message -> message.contains("Kraken Hatchling"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Welkin Tern"));
    }

    @Test
    void sphinxOnTopOfLibraryDoesNotRevealItself() {
        harness.setLibrary(player1, List.of(new SphinxOfJwarIsle()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).isNotEmpty()
                .noneMatch(message -> message.contains("Sphinx of Jwar Isle"));
        assertThat(harness.getConn2().getSentMessages()).isNotEmpty()
                .noneMatch(message -> message.contains("Sphinx of Jwar Isle"));
    }

    @Test
    void visibilityUpdatesWhenLibraryTopChanges() {
        harness.addToBattlefield(player1, new SphinxOfJwarIsle());
        harness.setLibrary(player1, List.of(new WelkinTern()));
        harness.publishState();
        harness.setLibrary(player1, List.of(new KrakenHatchling()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("Kraken Hatchling"))
                .noneMatch(message -> message.contains("Welkin Tern"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Kraken Hatchling"));
    }

    @Test
    void visibilityEndsWhenSphinxLeavesBattlefield() {
        harness.addToBattlefield(player1, new SphinxOfJwarIsle());
        harness.setLibrary(player1, List.of(new WelkinTern()));
        harness.publishState();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).isNotEmpty()
                .noneMatch(message -> message.contains("Welkin Tern"));
    }

    @Test
    void emptyLibraryHasNoTopCardToShow() {
        harness.addToBattlefield(player1, new SphinxOfJwarIsle());
        harness.setLibrary(player1, List.of());
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void shroudPreventsEitherPlayerFromTargetingSphinx() {
        var sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfJwarIsle());
        for (var player : List.of(player1, player2)) {
            harness.setHand(player, List.of(new IntoTheRoil()));
            harness.addMana(player, ManaColor.BLUE, 2);

            assertThatThrownBy(() -> harness.castInstant(player, 0, sphinx.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void groundCreatureCannotBlockSphinxButFlyingCreatureCan() {
        addCreatureReady(player1, new SphinxOfJwarIsle());
        harness.addToBattlefield(player2, new KrakenHatchling());
        harness.addToBattlefield(player2, new WelkinTern());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Welkin Tern");
        harness.assertOnBattlefield(player1, "Sphinx of Jwar Isle");
    }
}
