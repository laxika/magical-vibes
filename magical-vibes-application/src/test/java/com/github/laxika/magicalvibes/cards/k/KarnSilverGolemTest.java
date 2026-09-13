package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BullHippo;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HoppingAutomaton;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarnSilverGolem.class, BullHippo.class, Forest.class, HoppingAutomaton.class,
        WornPowerstone.class})
class KarnSilverGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked gives Karn -4/+4 until end of turn")
    void becomingBlockedGivesMinusFourPlusFour() {
        Permanent karn = addKarnReady(player1);
        karn.setAttacking(true);
        addReadyHippo(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(karn.getEffectivePower()).isEqualTo(0);
        assertThat(karn.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Blocking gives Karn -4/+4 until end of turn")
    void blockingGivesMinusFourPlusFour() {
        Permanent attacker = addReadyHippo(player2);
        attacker.setAttacking(true);
        Permanent karn = addKarnReady(player1);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(karn.getEffectivePower()).isEqualTo(0);
        assertThat(karn.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("An unblocked Karn gets no combat boost")
    void unblockedKarnGetsNoCombatBoost() {
        Permanent karn = addKarnReady(player1);
        karn.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(karn.getEffectivePower()).isEqualTo(4);
        assertThat(karn.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Animates a target noncreature artifact with P/T equal to its mana value")
    void animatesNoncreatureArtifact() {
        addKarnReady(player1);
        harness.addToBattlefield(player1, new WornPowerstone());
        Permanent powerstone = findPermanent(player1, "Worn Powerstone");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, powerstone.getId());
        harness.passBothPriorities();

        assertThat(powerstone.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, powerstone)).isTrue();
        assertThat(gqs.isArtifact(gd, powerstone)).isTrue();
        assertThat(powerstone.getEffectivePower()).isEqualTo(3);
        assertThat(powerstone.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Animates an opponent's target noncreature artifact")
    void animatesOpponentsNoncreatureArtifact() {
        addKarnReady(player1);
        harness.addToBattlefield(player2, new WornPowerstone());
        Permanent powerstone = findPermanent(player2, "Worn Powerstone");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, powerstone.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, powerstone)).isTrue();
        assertThat(gqs.isArtifact(gd, powerstone)).isTrue();
        assertThat(powerstone.getEffectivePower()).isEqualTo(3);
        assertThat(powerstone.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addKarnReady(player1);
        Permanent target = addCreatureReady(player2, new HoppingAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifactPermanent() {
        addKarnReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Animation is cleared at end of turn")
    void animationClearedAtEndOfTurn() {
        addKarnReady(player1);
        harness.addToBattlefield(player1, new WornPowerstone());
        Permanent powerstone = findPermanent(player1, "Worn Powerstone");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, powerstone.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, powerstone)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(powerstone.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, powerstone)).isFalse();
        assertThat(gqs.isArtifact(gd, powerstone)).isTrue();
    }

    @Test
    @DisplayName("Combat boost is cleared at end of turn")
    void combatBoostClearedAtEndOfTurn() {
        Permanent attacker = addReadyHippo(player2);
        attacker.setAttacking(true);
        Permanent karn = addKarnReady(player1);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(karn.getEffectiveToughness()).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(karn.getEffectivePower()).isEqualTo(4);
        assertThat(karn.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures gives Karn -4/+4 only once")
    void becomingBlockedByMultipleCreaturesGivesOneBoost() {
        Permanent karn = addKarnReady(player1);
        karn.setAttacking(true);
        addReadyHippo(player2);
        addReadyHippo(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(karn.getEffectivePower()).isEqualTo(0);
        assertThat(karn.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Blocking multiple creatures gives Karn -4/+4 only once")
    void blockingMultipleCreaturesGivesOneBoost() {
        KarnSilverGolem card = new KarnSilverGolem();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent karn = addCreatureReady(player1, card);
        Permanent firstAttacker = addReadyHippo(player2);
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addReadyHippo(player2);
        secondAttacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(karn.getEffectiveToughness()).isEqualTo(8);
        assertThat(karn.getEffectivePower()).isEqualTo(0);
    }

    private Permanent addKarnReady(Player player) {
        return addCreatureReady(player, new KarnSilverGolem());
    }

    private Permanent addReadyHippo(Player player) {
        return addCreatureReady(player, new BullHippo());
    }
}
