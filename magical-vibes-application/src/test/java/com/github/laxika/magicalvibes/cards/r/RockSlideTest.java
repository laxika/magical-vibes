package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.h.HulkingCyclops;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RockSlide.class, RiverBoa.class, CloudElemental.class, HulkingCyclops.class})
class RockSlideTest extends BaseCardTest {

    private void prepareMana() {
        harness.setHand(player1, List.of(new RockSlide()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Divides X damage among attacking creatures without flying")
    void dividesAmongAttackers() {
        Permanent first = addCreatureReady(player2, new RiverBoa());
        Permanent second = addCreatureReady(player2, new RiverBoa());
        first.setAttacking(true);
        second.setAttacking(true);
        prepareMana();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstantForX(player1, 0, 4, Map.of(first.getId(), 2, second.getId(), 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "River Boa");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target a blocking creature without flying")
    void targetsBlocker() {
        Permanent attacker = addCreatureReady(player1, new RiverBoa());
        Permanent blocker = addCreatureReady(player2, new RiverBoa());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        prepareMana();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstantForX(player1, 0, 2, Map.of(blocker.getId(), 2));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "River Boa");
    }

    @Test
    @DisplayName("Rejects non-attacking, non-blocking creatures")
    void rejectsIdleCreature() {
        Permanent idle = addCreatureReady(player2, new RiverBoa());
        prepareMana();

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 2, Map.of(idle.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects attacking creatures with flying")
    void rejectsFlyingAttacker() {
        Permanent flyer = addCreatureReady(player2, new CloudElemental());
        flyer.setAttacking(true);
        prepareMana();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 2, Map.of(flyer.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Assignments must sum to X")
    void assignmentsMustSumToX() {
        Permanent attacker = addCreatureReady(player2, new RiverBoa());
        attacker.setAttacking(true);
        prepareMana();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 3, Map.of(attacker.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Applies each chosen damage amount to its target")
    void appliesChosenDamageAmounts() {
        Permanent first = addCreatureReady(player2, new HulkingCyclops());
        Permanent second = addCreatureReady(player2, new HulkingCyclops());
        first.setAttacking(true);
        second.setAttacking(true);
        prepareMana();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstantForX(player1, 0, 3, Map.of(first.getId(), 1, second.getId(), 2));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Allows zero targets when X is zero")
    void allowsZeroTargetsWhenXIsZero() {
        Permanent idle = addCreatureReady(player2, new RiverBoa());
        prepareMana();

        harness.castInstantForX(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rock Slide");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(idle);
    }

    @Test
    @DisplayName("Does nothing when its only target stops attacking before resolution")
    void ignoresTargetThatStopsAttackingBeforeResolution() {
        Permanent attacker = addCreatureReady(player2, new RiverBoa());
        attacker.setAttacking(true);
        prepareMana();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstantForX(player1, 0, 2, Map.of(attacker.getId(), 2));
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }
}
