package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExplosiveGrowth;
import com.github.laxika.magicalvibes.cards.h.HoodedKavu;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinalEmbrace.class, HoodedKavu.class, ExplosiveGrowth.class, Repulse.class})
class SpinalEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("During combat, Spinal Embrace untaps, steals, and grants haste to the target")
    void resolvesCombatControlEffect() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent target = addCreatureReady(player2, new HoodedKavu());
        target.tap();

        castSpinalEmbrace(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The creature is sacrificed at the next end step and its toughness becomes life gained")
    void sacrificesCreatureAndGainsLifeAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent target = addCreatureReady(player2, new HoodedKavu());

        castSpinalEmbrace(target.getId());
        harness.passBothPriorities();
        int lifeBeforeEndStep = gd.getLife(player1.getId());

        advanceToNextEndStep();

        harness.assertNotOnBattlefield(player1, "Hooded Kavu");
        harness.assertNotOnBattlefield(player2, "Hooded Kavu");
        harness.assertInGraveyard(player2, "Hooded Kavu");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeEndStep + 2);
    }

    @Test
    @DisplayName("Life gain uses the creature's toughness at the next end step")
    void gainsLifeEqualToCurrentToughnessAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent target = addCreatureReady(player2, new HoodedKavu());

        castSpinalEmbrace(target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        int lifeBeforeEndStep = gd.getLife(player1.getId());

        advanceToNextEndStep();

        harness.assertInGraveyard(player2, "Hooded Kavu");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeEndStep + 4);
    }

    @Test
    @DisplayName("The delayed sacrifice does nothing if the creature leaves the battlefield first")
    void doesNotSacrificeOrGainLifeIfTargetLeavesBeforeEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent target = addCreatureReady(player2, new HoodedKavu());

        castSpinalEmbrace(target.getId());
        harness.passBothPriorities();
        int lifeBeforeEndStep = gd.getLife(player1.getId());

        harness.setHand(player2, List.of(new Repulse()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInHand(player2, "Hooded Kavu");
        advanceToNextEndStep();

        harness.assertInHand(player2, "Hooded Kavu");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeEndStep);
    }

    @Test
    @DisplayName("Spinal Embrace cannot target a creature already controlled by its caster")
    void cannotTargetOwnCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent ownCreature = addCreatureReady(player1, new HoodedKavu());
        setUpSpinalEmbrace();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Spinal Embrace cannot be cast outside combat")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent target = addCreatureReady(player2, new HoodedKavu());
        setUpSpinalEmbrace();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void setUpSpinalEmbrace() {
        harness.setHand(player1, List.of(new SpinalEmbrace()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void castSpinalEmbrace(UUID targetId) {
        setUpSpinalEmbrace();
        harness.castInstant(player1, 0, targetId);
    }

    private void advanceToNextEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
