package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.g.GixianInfiltrator;
import com.github.laxika.magicalvibes.cards.i.InvisibleStalker;
import com.github.laxika.magicalvibes.cards.s.SpideryGrasp;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TributeToHunger.class, GrizzlyBears.class, GiantSpider.class, AvacynsPilgrim.class,
        InvisibleStalker.class, SpideryGrasp.class, AssaultSuit.class, GixianInfiltrator.class})
class TributeToHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Tribute to Hunger targeting opponent puts instant on the stack")
    void castingPutsInstantOnStack() {
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(TributeToHunger.class);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent with one creature sacrifices it and controller gains life equal to toughness")
    void opponentSacrificesCreatureAndControllerGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Grizzly Bears has toughness 2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose")
    void opponentWithMultipleCreaturesChooses() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness.class);
    }

    @Test
    @DisplayName("Opponent chooses Giant Spider — controller gains 4 life (toughness 4)")
    void opponentChoosesHighToughnessCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Player 2 chooses to sacrifice Giant Spider (toughness 4)
        harness.handlePermanentChosen(player2, giant.getId());

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Opponent chooses Grizzly Bears — controller gains 2 life (toughness 2)")
    void opponentChoosesLowToughnessCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Player 2 chooses to sacrifice Grizzly Bears (toughness 2)
        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("No sacrifice and no life gain when opponent has no creatures")
    void noCreaturesNoLifeGain() {
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gameLogContains("no creatures to sacrifice")).isTrue();
    }

    @Test
    @DisplayName("Tribute to Hunger goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tribute to Hunger");
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hexproofCreatureCanBeSacrificed() {
        harness.addToBattlefield(player2, new InvisibleStalker());
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Invisible Stalker");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainsLifeUsingToughnessBeforeTemporaryBoostEnds() {
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player2, new AvacynsPilgrim());
        harness.setHand(player2, List.of(new SpideryGrasp()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, pilgrim.getId());
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Avacyn's Pilgrim");
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
    }

    @Test
    void creatureThatCannotBeSacrificedIsNotSacrificed() {
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player2, new AvacynsPilgrim());
        Permanent suit = harness.addToBattlefieldAndReturn(player2, new AssaultSuit());
        suit.setAttachedTo(pilgrim.getId());
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Avacyn's Pilgrim");
        harness.assertNotInGraveyard(player2, "Avacyn's Pilgrim");
        harness.assertLife(player1, 20);
    }

    @Test
    void chosenSacrificeTriggersOtherPermanents() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player2, new GixianInfiltrator());
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player2, new AvacynsPilgrim());
        harness.setHand(player1, List.of(new TributeToHunger()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, pilgrim.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Avacyn's Pilgrim");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(infiltrator.getPlusOnePlusOneCounters()).isEqualTo(1);
    }
}
