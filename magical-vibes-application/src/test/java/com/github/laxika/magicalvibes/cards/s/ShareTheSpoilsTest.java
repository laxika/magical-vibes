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
        Forest replacement = new Forest();
        GrizzlyBears opponentSpell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(player1Top, replacement));
        harness.setLibrary(player2, List.of(opponentSpell));
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, opponentSpell.getId());
        resolveAllTriggers();

        ExiledCardEntry newlyExiled = gd.findExiledCard(replacement.getId());
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

    @Test
    void opponentMayPlayALandAndExilesFromTheirOwnLibrary() {
        Forest land = new Forest();
        GrizzlyBears replacement = new GrizzlyBears();
        GrizzlyBears controllerTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(controllerTop));
        harness.setLibrary(player2, List.of(land, replacement));
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        prepareMainPhase(player2);
        harness.castFromExile(player2, land.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(replacement.getId()).sourcePermanentId())
                .isEqualTo(findPermanent(player1, "Share the Spoils").getId());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void castingASpellWithOtherColorsUsesThePermissionForLandsToo() {
        GrizzlyBears spell = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(spell, land));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
    }

    @Test
    void inactivePlayerCannotUseThePermission() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        prepareMainPhase(player1);
        assertThatThrownBy(() -> harness.castFromExile(player2, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    void castingFromHandDoesNotReplenishExileOrUseThePermission() {
        GrizzlyBears exiledSpell = new GrizzlyBears();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(exiledSpell, nextCard));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        prepareMainPhase(player1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();
        assertThat(gd.findExiledCard(nextCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, exiledSpell.getId());
        resolveAllTriggers();
        assertThat(gd.findExiledCard(nextCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(exiledSpell.getId())).isNull();
    }

    @Test
    void leavingAndReturningDoesNotRestoreAccessToPreviouslyExiledCards() {
        Forest oldLand = new Forest();
        Forest newLand = new Forest();
        harness.setLibrary(player1, List.of(oldLand, newLand));
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        var source = findPermanent(player1, "Share the Spoils");
        harness.getPermanentRemovalService().removePermanentToHand(gd, source);
        prepareMainPhase(player1);
        assertThatThrownBy(() -> harness.castFromExile(player1, oldLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");

        harness.castFromHand(player1, source.getCard(), "{1}{R}");
        resolveAllTriggers();
        assertThatThrownBy(() -> harness.castFromExile(player1, oldLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
        harness.castFromExile(player1, newLand.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(oldLand.getId())).isNotNull();
        assertThat(gd.findExiledCard(newLand.getId())).isNull();
    }

    @Test
    void emptyLibrariesDoNotPreventTheEnchantmentFromResolving() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Share the Spoils");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void permissionDoesNotAllowCreatureSpellsOutsideAMainPhase() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(spell));
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, new ShareTheSpoils(), "{1}{R}");
        resolveAllTriggers();

        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();

        prepareMainPhase(player1);
        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
