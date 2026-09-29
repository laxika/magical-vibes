package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShareTheSpoils.class, Forest.class, GrizzlyBears.class})
class ShareTheSpoilsTest extends BaseCardTest {

    @Test
    void exilesTheTopCardOfEachLibraryWhenItEnters() {
        GrizzlyBears player1Top = new GrizzlyBears();
        Forest player2Top = new Forest();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of(player2Top));

        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        assertThat(gd.findExiledCard(player1Top.getId()).sourcePermanentId())
                .isEqualTo(findPermanent(player1, "Share the Spoils").getId());
        assertThat(gd.findExiledCard(player2Top.getId()).sourcePermanentId())
                .isEqualTo(findPermanent(player1, "Share the Spoils").getId());
    }

    @Test
    void activePlayerMayCastAnOpponentsExiledSpellAndExilesTheirTopCard() {
        GrizzlyBears player1Top = new GrizzlyBears();
        GrizzlyBears opponentSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Top));
        harness.setLibrary(player2, List.of(opponentSpell));
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, opponentSpell.getId());
        resolveAllTriggers();

        ExiledCardEntry newlyExiled = gd.findExiledCard(player1Top.getId());
        assertThat(newlyExiled).isNotNull();
        assertThat(newlyExiled.sourcePermanentId())
                .isEqualTo(findPermanent(player1, "Share the Spoils").getId());
    }

    @Test
    void playingALandUsesTheOneCardPerTurnPermission() {
        Forest land = new Forest();
        GrizzlyBears nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, nextCard));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        prepareMainPhase(player1);
        harness.castFromExile(player1, land.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(nextCard.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, nextCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
