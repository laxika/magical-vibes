package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EndBlazeEpiphany.class, AirElemental.class, Forest.class, GrizzlyBears.class, Shock.class, Unsummon.class})
class EndBlazeEpiphanyTest extends BaseCardTest {

    private void resolveUntilChoiceOrDone() {
        int guard = 0;
        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Deals X damage and lets you play one card among the cards exiled for the dying creature's power")
    void exilesCardsEqualToDyingPowerAndGrantsChosenCardPermission() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card first = new Shock();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveUntilChoiceOrDone();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
    }

    @Test
    @DisplayName("Does not exile cards when the targeted creature survives")
    void survivingCreatureDoesNotTriggerExile() {
        harness.addToBattlefield(player2, new AirElemental());
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Air Elemental"));
        resolveUntilChoiceOrDone();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Zero damage still creates a trigger for a creature that dies later this turn")
    void zeroDamageStillTriggersOnLaterDeath() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card first = new Forest();
        Card second = new EndBlazeEpiphany();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new EndBlazeEpiphany(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, 0, targetId);
        resolveUntilChoiceOrDone();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, targetId);
        resolveUntilChoiceOrDone();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
    }

    @Test
    @DisplayName("Exiles the available cards when the library is smaller than the dying creature's power")
    void shortLibraryStillAllowsChoosingAndCastingItsOnlyCard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveUntilChoiceOrDone();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        harness.handleMultipleCardsChosen(player1, List.of(top.getId()));
        harness.castFromExile(player1, top.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("An empty library does not request a card choice")
    void emptyLibraryDoesNotRequestChoice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveUntilChoiceOrDone();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature returned to hand and recast is no longer tracked by the delayed trigger")
    void returnedAndRecastCreatureDoesNotTriggerExile() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new EndBlazeEpiphany(), new Unsummon(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        var originalId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, 0, originalId);
        resolveUntilChoiceOrDone();
        harness.castAndResolveInstant(player1, 0, originalId);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.castCreature(player1, 1);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        resolveUntilChoiceOrDone();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Permission persists through your next end step and expires after that turn")
    void permissionExpiresAfterControllersNextTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card chosen = new Shock();
        harness.setLibrary(player1, List.of(chosen, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveUntilChoiceOrDone();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, chosen.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen);
    }

    @Test
    @DisplayName("A target returned to hand before resolution prevents damage and the delayed trigger")
    void targetLeavingBeforeResolutionDoesNotExileCards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new EndBlazeEpiphany()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, 2, targetId);
        harness.castInstant(player2, 0, targetId);
        resolveUntilChoiceOrDone();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "End-Blaze Epiphany");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

}
