package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarkovWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Has ETB target creatures can't block effect")
    void hasEtbEffect() {
        MarkovWarlord card = new MarkovWarlord();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(TargetCreatureCantBlockThisTurnEffect.class);
    }

    @Test
    @DisplayName("ETB makes up to two target creatures unable to block this turn")
    void etbMakesTwoTargetsUnableToBlock() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMarkovWarlord(List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(creature1.isCantBlockThisTurn()).isTrue();
        assertThat(creature2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("ETB can target one creature")
    void etbCanTargetOneCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMarkovWarlord(List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("ETB can choose no targets")
    void etbCanChooseNoTargets() {
        castMarkovWarlord(List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Markov Warlord");
    }

    @Test
    @DisplayName("Cannot target more than two creatures")
    void cannotTargetMoreThanTwoCreatures() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creature3 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castMarkovWarlord(List.of(
                creature1.getId(),
                creature2.getId(),
                creature3.getId()
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> castMarkovWarlord(List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeted creature cannot declare as blocker after ETB resolves")
    void targetedCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        castMarkovWarlord(List.of(blocker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMarkovWarlord(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new MarkovWarlord()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, targetIds);
    }
}
