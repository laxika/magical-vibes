package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.ExpandedAnatomy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TwinscrollShaman;
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

@CardUsed({SymmetrySage.class, BarkshellBlessing.class, GrizzlyBears.class, HillGiant.class,
        Shock.class, ExpandedAnatomy.class, TwinscrollShaman.class})
class SymmetrySageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant sets a creature's base power to 2 until end of turn")
    void castingInstantSetsTargetBasePower() {
        harness.addToBattlefield(player1, new SymmetrySage());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Copying an instant triggers Symmetry Sage")
    void copyingInstantSetsTargetBasePower() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SymmetrySage());
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, conspireA.getId(), List.of(conspireA.getId(), conspireB.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sage.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The magecraft effect wears off at end of turn")
    void basePowerSetWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SymmetrySage());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The magecraft effect cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new SymmetrySage());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void castingCreatureDoesNotTriggerMagecraft() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SymmetrySage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(sage.getEffectivePower()).isZero();
    }

    @Test
    @DisplayName("Sorcery magecraft can target the Sage and preserves counters above base power")
    void sorceryTriggersApplyBelowCountersAndDoNotStackPowerIncreases() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SymmetrySage());
        harness.setHand(player1, List.of(new ExpandedAnatomy(), new ExpandedAnatomy()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, sage.getId());
        harness.handlePermanentChosen(player1, sage.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(2);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);

        harness.castSorcery(player1, 0, sage.getId());
        harness.handlePermanentChosen(player1, sage.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's sorcery does not trigger the Sage")
    void opponentCastingSorceryDoesNotTriggerMagecraft() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SymmetrySage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ExpandedAnatomy()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player2, 0, sage.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, sage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sage)).isEqualTo(4);
    }

    @Test
    @DisplayName("Magecraft does not affect a target that leaves before the trigger resolves")
    void removedTargetDoesNotReceiveMagecraftEffect() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SymmetrySage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, sage.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sage.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Symmetry Sage");
        harness.assertInGraveyard(player1, "Symmetry Sage");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A ground creature cannot block Symmetry Sage")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent sage = addCreatureReady(player1, new SymmetrySage());
        Permanent blocker = addCreatureReady(player2, new TwinscrollShaman());

        assertThat(bls.canBlockAttacker(gd, blocker, sage,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }
}
