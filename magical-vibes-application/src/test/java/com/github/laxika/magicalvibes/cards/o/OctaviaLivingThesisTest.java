package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OctaviaLivingThesis.class, BarkshellBlessing.class, GiantGrowth.class,
        GrizzlyBears.class, HillGiant.class, ProdigalPyromancer.class, RampantGrowth.class, Shock.class})
class OctaviaLivingThesisTest extends BaseCardTest {

    @Test
    @DisplayName("costs eight less with eight instant or sorcery cards in the graveyard")
    void costsEightLessWithEightInstantOrSorceryCards() {
        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new OctaviaLivingThesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Octavia, Living Thesis");
    }

    @Test
    @DisplayName("does not reduce its cost for non-instant and non-sorcery graveyard cards")
    void doesNotReduceCostForOtherCards() {
        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new OctaviaLivingThesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("magecraft sets any target creature's base power and toughness to 8/8")
    void magecraftSetsTargetCreatureBasePowerAndToughness() {
        addCreatureReady(player1, new OctaviaLivingThesis());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
    }

    @Test
    @DisplayName("copying an instant also triggers magecraft")
    void copyingInstantTriggersMagecraft() {
        addCreatureReady(player1, new OctaviaLivingThesis());
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(12);
    }

    @Test
    @DisplayName("ward counters an opponent's spell when they do not pay eight")
    void wardCountersUnpaidSpell() {
        Permanent octavia = addCreatureReady(player1, new OctaviaLivingThesis());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, octavia.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("paying ward eight lets an opponent's spell resolve")
    void payingWardEightLetsSpellResolve() {
        Permanent octavia = addCreatureReady(player1, new OctaviaLivingThesis());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        harness.castInstant(player2, 0, octavia.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, octavia)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, octavia)).isEqualTo(11);
    }

    @Test
    void mixedInstantsAndSorceriesMeetTheThreshold() {
        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new RampantGrowth(), new RampantGrowth(), new RampantGrowth(), new RampantGrowth()));
        harness.setHand(player1, List.of(new OctaviaLivingThesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Octavia, Living Thesis");
    }

    @Test
    void costReductionDoesNotRemoveBlueManaRequirements() {
        harness.setGraveyard(player1, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new OctaviaLivingThesis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsGraveyardDoesNotReduceCost() {
        harness.setGraveyard(player2, List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new OctaviaLivingThesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayFullCostBelowThreshold() {
        harness.setHand(player1, List.of(new OctaviaLivingThesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Octavia, Living Thesis");
    }

    @Test
    void creatureSpellDoesNotTriggerMagecraft() {
        addCreatureReady(player1, new OctaviaLivingThesis());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void magecraftExpiresAtEndOfTurn() {
        addCreatureReady(player1, new OctaviaLivingThesis());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void opponentsSpellDoesNotTriggerMagecraft() {
        addCreatureReady(player1, new OctaviaLivingThesis());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, target.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void castingSorceryTriggersMagecraft() {
        addCreatureReady(player1, new OctaviaLivingThesis());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
    }

    @Test
    void controllersSpellDoesNotRequireWardPayment() {
        Permanent octavia = addCreatureReady(player1, new OctaviaLivingThesis());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, octavia.getId());
        harness.handlePermanentChosen(player1, octavia.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, octavia)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, octavia)).isEqualTo(11);
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent octavia = addCreatureReady(player1, new OctaviaLivingThesis());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, octavia.getId());
        resolveAllTriggers();

        assertThat(octavia.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
