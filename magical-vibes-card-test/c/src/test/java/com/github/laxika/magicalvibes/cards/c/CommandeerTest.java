package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Commandeer.class, Boomerang.class, Counterspell.class, GrizzlyBears.class,
        LavaAxe.class, LeoninScimitar.class})
class CommandeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a noncreature permanent spell")
    void gainsControlOfNoncreaturePermanentSpell() {
        LeoninScimitar scimitar = new LeoninScimitar();
        harness.setHand(player2, List.of(new Commandeer(), new Counterspell(), new Boomerang()));
        harness.castFromHand(player1, scimitar, "{1}");
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, scimitar.getId(), List.of(1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(harness.getGameData().exiledCards)
                .extracting(exiled -> exiled.card().getName())
                .containsExactlyInAnyOrder("Counterspell", "Boomerang");
    }

    @Test
    @DisplayName("Can choose new targets for the spell it controls")
    void canChooseNewTargets() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Commandeer(), new Counterspell(), new Boomerang()));
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, lavaAxe.getId(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
        harness.assertInGraveyard(player1, "Lava Axe");
        harness.assertInGraveyard(player2, "Commandeer");
    }

    @Test
    @DisplayName("May keep the spell's original targets")
    void mayKeepOriginalTargets() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Commandeer(), new Counterspell(), new Boomerang()));
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, lavaAxe.getId(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 5);
        harness.assertInGraveyard(player1, "Lava Axe");
        harness.assertInGraveyard(player2, "Commandeer");
    }

    @Test
    @DisplayName("Alternate cost requires two blue cards")
    void alternateCostRequiresBlueCards() {
        LeoninScimitar scimitar = new LeoninScimitar();
        harness.setHand(player2, List.of(new Commandeer(), new GrizzlyBears(), new Counterspell()));
        harness.castFromHand(player1, scimitar, "{1}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player2, 0, scimitar.getId(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blue");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Commandeer(), new Counterspell(), new Boomerang()));
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player2, 0, bears.getId(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay the normal mana cost without exiling cards")
    void canPayNormalManaCost() {
        LeoninScimitar scimitar = new LeoninScimitar();
        harness.castFromHand(player1, scimitar, "{1}");
        harness.setHand(player2, List.of(new Commandeer()));
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, scimitar.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player2, "Commandeer");
    }

    @Test
    @DisplayName("Cannot exile Commandeer itself to pay its alternate cost")
    void cannotExileItselfForAlternateCost() {
        LeoninScimitar scimitar = new LeoninScimitar();
        harness.castFromHand(player1, scimitar, "{1}");
        harness.setHand(player2, List.of(new Commandeer(), new Counterspell()));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player2, 0, scimitar.getId(), List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Cannot exile the same blue card twice")
    void cannotExileSameCardTwice() {
        LeoninScimitar scimitar = new LeoninScimitar();
        harness.castFromHand(player1, scimitar, "{1}");
        harness.setHand(player2, List.of(new Commandeer(), new Counterspell(), new Boomerang()));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player2, 0, scimitar.getId(), List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Can retarget a stolen counterspell to the resolving Commandeer")
    void canRetargetCounterspellToResolvingCommandeer() {
        LeoninScimitar scimitar = new LeoninScimitar();
        var scimitarPermanent = harness.addToBattlefieldAndReturn(player1, scimitar);
        Boomerang boomerang = new Boomerang();
        Commandeer commandeer = new Commandeer();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(boomerang, commandeer));
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, scimitarPermanent.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.castInstant(player1, 0, counterspell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(commandeer.getId());
        harness.handlePermanentChosen(player1, commandeer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Boomerang");
        harness.assertInGraveyard(player1, "Commandeer");
        harness.assertInGraveyard(player2, "Counterspell");
    }
}
