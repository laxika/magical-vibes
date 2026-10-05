package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsochronScepter;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeeraWildMage.class, Pyroclasm.class, Forest.class, GrizzlyBears.class,
        Cancel.class, IsochronScepter.class, Boomerang.class})
class NeeraWildMageTest extends BaseCardTest {

    @Test
    @DisplayName("Accepted trigger puts the spell on the library bottom and offers a nonland card for free")
    void acceptedTriggerPutsSpellOnBottomAndCastsRevealedCard() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm spell = new Pyroclasm();
        GrizzlyBears revealedCreature = new GrizzlyBears();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new Forest(), revealedCreature));

        harness.castFromHand(player1, spell, "{1}{R}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Declining Neera leaves the cast spell to resolve normally")
    void decliningTriggerLeavesSpellAlone() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm spell = new Pyroclasm();
        Forest forest = new Forest();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(forest));

        harness.castFromHand(player1, spell, "{1}{R}");
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("The bottoming choice is made when Neera's ability resolves, after a response window")
    void bottomingChoiceWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm spell = new Pyroclasm();

        harness.castFromHand(player1, spell, "{1}{R}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Declining the revealed nonland returns it and the revealed lands below unrevealed cards")
    void decliningRevealedCardReturnsAllRevealedCardsToBottom() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm spell = new Pyroclasm();
        Forest revealedLand = new Forest();
        GrizzlyBears revealedCreature = new GrizzlyBears();
        Forest unrevealedLand = new Forest();
        harness.setLibrary(player1, List.of(revealedLand, revealedCreature, unrevealedLand));

        harness.castFromHand(player1, spell, "{1}{R}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(unrevealedLand, spell);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .containsExactlyInAnyOrder(revealedLand, revealedCreature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("If the library contains only lands, Neera can reveal and recast the bottomed spell")
    void revealCanReachTheOriginalSpellOnTheBottom() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm spell = new Pyroclasm();
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        harness.castFromHand(player1, spell, "{1}{R}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the first spell still uses Neera's once-per-turn trigger")
    void decliningFirstSpellPreventsTriggerOnSecondSpell() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm firstSpell = new Pyroclasm();
        Pyroclasm secondSpell = new Pyroclasm();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(firstSpell, secondSpell));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstSpell, secondSpell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Neera can trigger again during the next player's turn")
    void triggerResetsOnOpponentsTurn() {
        var neera = harness.addToBattlefieldAndReturn(player1, new NeeraWildMage());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, neera.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Neera, Wild Mage");
        assertThat(gd.playerDecks.get(player1.getId())).anyMatch(card -> card instanceof Boomerang);
    }

    @Test
    @DisplayName("A spell countered before Neera resolves cannot be bottomed and causes no reveal")
    void counteredTriggeringSpellDoesNotRevealCards() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm spell = new Pyroclasm();
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castFromHand(player1, spell, "{1}{R}");
        harness.handleMayAbilityChosen(player1, true);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A copy cast through Isochron Scepter can be bottomed to reveal a nonland")
    void castCopyCanBeBottomedToRevealAnotherSpell() {
        harness.setHand(player1, List.of(new IsochronScepter(), new Boomerang()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        var neera = harness.addToBattlefieldAndReturn(player1, new NeeraWildMage());
        GrizzlyBears revealedCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), revealedCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, neera.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Neera, Wild Mage");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card instanceof Boomerang);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof Boomerang);
    }
}
