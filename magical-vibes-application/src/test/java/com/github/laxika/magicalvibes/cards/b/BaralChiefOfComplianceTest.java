package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.Disallow;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SirenStormtamer;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({BaralChiefOfCompliance.class, Counterspell.class, Disallow.class, Divination.class,
        Forest.class, GrizzlyBears.class, NarsetParterOfVeils.class, Shock.class, SirenStormtamer.class})
class BaralChiefOfComplianceTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells you cast cost {1} less")
    void instantAndSorcerySpellsCostOneLess() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("Creature spells are not reduced")
    void creatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When you counter a spell, you may draw and discard")
    void counteringSpellMayDrawAndDiscard() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        Counterspell counterspell = new Counterspell();
        GrizzlyBears discardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(counterspell, discardCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gameData.playerGraveyards.get(player2.getId())).contains(shock);
    }

    @Test
    @DisplayName("When you counter a spell, you may decline to draw")
    void counteringSpellMayDeclineDraw() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new Counterspell(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
    }

    @Test
    @DisplayName("An instant's generic cost is reduced, but its colored cost remains")
    void instantGenericCostIsReduced() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        Shock shock = new Shock();
        Disallow disallow = new Disallow();
        harness.setHand(player2, List.of(shock));
        harness.setHand(player1, List.of(disallow));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.castInstant(player1, 0, shock.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == disallow);
    }

    @Test
    @DisplayName("Cost reduction cannot replace a colored mana requirement")
    void coloredManaRequirementIsNotReduced() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's spells do not receive Baral's cost reduction")
    void opponentSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent countering your spell does not trigger your Baral")
    void opponentCounterDoesNotTriggerBaral() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("An activated ability you control countering a spell triggers Baral")
    void activatedAbilityCounterTriggersBaral() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        harness.addToBattlefield(player1, new SirenStormtamer());
        Shock shock = new Shock();
        Shock drawnCard = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 1, null, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        harness.assertInGraveyard(player1, "Siren Stormtamer");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Countering an activated ability does not trigger Baral")
    void counteringAbilityDoesNotTriggerBaral() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        SirenStormtamer stormtamer = new SirenStormtamer();
        harness.addToBattlefield(player2, stormtamer);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new Disallow()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, shock.getId());
        if (player2.getId().equals(gqs.getPriorityPlayerId(gd))) {
            harness.passPriority(player2);
        }
        harness.castInstant(player1, 0, gd.stack.getLast().getTargetableId());

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(shock);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Countering your own spell also triggers Baral and allows discarding the drawn card")
    void counteringOwnSpellCanDiscardDrawnCard() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        Shock shock = new Shock();
        Shock retainedCard = new Shock();
        Shock drawnCard = new Shock();
        harness.setHand(player1, List.of(shock, new Disallow(), retainedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, shock.getId());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, drawnCard);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A prohibited optional draw cannot be chosen and does not cause a discard")
    void drawRestrictionPreventsOptionalDrawAndDiscard() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        harness.addToBattlefield(player2, new NarsetParterOfVeils());
        Shock shock = new Shock();
        Shock firstDraw = new Shock();
        Shock blockedDraw = new Shock();
        harness.setHand(player1, List.of(new Disallow()));
        harness.setHand(player2, List.of(shock));
        harness.setLibrary(player1, List.of(firstDraw, blockedDraw));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(blockedDraw);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
    }
}
