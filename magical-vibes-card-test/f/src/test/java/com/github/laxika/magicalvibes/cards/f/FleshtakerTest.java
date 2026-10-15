package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HamletCaptain;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fleshtaker.class, HamletCaptain.class, TravelersAmulet.class, VillageRites.class})
class FleshtakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gains 1 life and scries 1")
    void sacrificeTriggersLifeGainAndScry() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        addCreatureReady(player1, new HamletCaptain());
        harness.setLibrary(player1, List.of(new HamletCaptain()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Only one other creature -> auto-sacrificed as the {1} cost.
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(fleshtaker.getPowerModifier()).isEqualTo(0);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(0);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hamlet Captain");
        assertThat(fleshtaker.getPowerModifier()).isEqualTo(2);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The {1} ability gives +2/+2 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        addCreatureReady(player1, new HamletCaptain());
        harness.setLibrary(player1, List.of(new HamletCaptain()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        assertThat(fleshtaker.getPowerModifier()).isEqualTo(2);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+2 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        addCreatureReady(player1, new HamletCaptain());
        harness.setLibrary(player1, List.of(new HamletCaptain()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        assertThat(fleshtaker.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fleshtaker.getPowerModifier()).isEqualTo(0);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The {1} ability cannot be activated without another creature")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new Fleshtaker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a non-creature does not trigger the life gain")
    void nonCreatureSacrificeDoesNotTrigger() {
        addCreatureReady(player1, new Fleshtaker());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Traveler's Amulet");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing Fleshtaker itself does not gain life or scry")
    void sacrificingSelfDoesNotTrigger() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        harness.setHand(player1, List.of(new VillageRites()));
        harness.setLibrary(player1, List.of(new HamletCaptain(), new HamletCaptain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstantWithSacrifice(player1, 0, null, fleshtaker.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Fleshtaker");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing another creature to a spell scries before drawing")
    void sacrificeToAnotherCardTriggersAndCanBottomTopCard() {
        addCreatureReady(player1, new Fleshtaker());
        Permanent creature = addCreatureReady(player1, new HamletCaptain());
        HamletCaptain top = new HamletCaptain();
        TravelersAmulet second = new TravelersAmulet();
        VillageRites third = new VillageRites();
        harness.setHand(player1, List.of(new VillageRites()));
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("An empty library does not prevent the sacrifice life gain or boost")
    void emptyLibraryStillGainsLifeAndBoosts() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        addCreatureReady(player1, new HamletCaptain());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(fleshtaker.getPowerModifier()).isEqualTo(2);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent sacrificing a creature does not trigger Fleshtaker")
    void opponentSacrificeDoesNotTrigger() {
        addCreatureReady(player1, new Fleshtaker());
        Permanent creature = addCreatureReady(player2, new HamletCaptain());
        harness.setHand(player2, List.of(new VillageRites()));
        harness.setLibrary(player2, List.of(new HamletCaptain(), new HamletCaptain()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstantWithSacrifice(player2, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ability requires mana even when another creature is available")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new Fleshtaker());
        addCreatureReady(player1, new HamletCaptain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Hamlet Captain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Repeated activations stack and do not require tapping Fleshtaker")
    void repeatedActivationsStackWhileTapped() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        fleshtaker.tap();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        addCreatureReady(player1, new HamletCaptain());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        addCreatureReady(player1, new HamletCaptain());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(fleshtaker.getPowerModifier()).isEqualTo(4);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Hamlet Captain");
    }
}
