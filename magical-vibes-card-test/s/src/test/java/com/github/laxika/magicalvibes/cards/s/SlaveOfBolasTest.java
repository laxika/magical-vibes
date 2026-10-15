package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlaveOfBolas.class, GrizzlyBears.class, Pacifism.class, RayOfCommand.class})
class SlaveOfBolasTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Slave of Bolas puts it on the stack targeting the creature")
    void castingPutsOnStack() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSlaveOfBolas(target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving gains control of the target, untaps it, and grants haste")
    void resolvesGainControlUntapAndHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        castSlaveOfBolas(target.getId());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The stolen creature is sacrificed at the beginning of the next end step, going to its owner's graveyard")
    void sacrificesStolenCreatureAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSlaveOfBolas(target.getId());
        harness.passBothPriorities();

        // Still controlled by player1 during the main phase.
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));

        harness.passUntil(TurnStep.END_STEP);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new GrizzlyBears()); // valid target so the spell is playable
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        setUpSlaveOfBolas();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void delayedSacrificeUsesTheSpellAsItsSource() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSlaveOfBolas(target.getId());
        StackEntry spell = gd.stack.getFirst();
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell.getCard());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    void cannotSacrificeCreatureTakenByOpponentBeforeEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSlaveOfBolas(target.getId());
        harness.passBothPriorities();
        takeCreatureBackWithRayOfCommand(target);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotSacrificeCreatureTakenInResponseToDelayedTrigger() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSlaveOfBolas(target.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        takeCreatureBackWithRayOfCommand(target);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void canTargetAndSacrificeOwnCreature() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        castSlaveOfBolas(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void missingTargetDoesNotScheduleSacrificeForAnotherCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent survivor = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        castSlaveOfBolas(target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    private void takeCreatureBackWithRayOfCommand(Permanent target) {
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void setUpSlaveOfBolas() {
        harness.setHand(player1, List.of(new SlaveOfBolas()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void castSlaveOfBolas(java.util.UUID targetId) {
        setUpSlaveOfBolas();
        harness.castSorcery(player1, 0, targetId);
    }
}
