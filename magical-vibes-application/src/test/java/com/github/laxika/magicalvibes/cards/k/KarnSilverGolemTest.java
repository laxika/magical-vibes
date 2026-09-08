package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KarnSilverGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked gives Karn -4/+4 until end of turn")
    void becomingBlockedGivesMinusFourPlusFour() {
        Permanent karn = addKarnReady(player1);
        karn.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(karn.getEffectivePower()).isEqualTo(0);
        assertThat(karn.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Blocking gives Karn -4/+4 until end of turn")
    void blockingGivesMinusFourPlusFour() {
        Permanent attacker = addReadyBears(player2);
        attacker.setAttacking(true);
        Permanent karn = addKarnReady(player1);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(karn.getEffectivePower()).isEqualTo(0);
        assertThat(karn.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Animates a target noncreature artifact with P/T equal to its mana value")
    void animatesNoncreatureArtifact() {
        addKarnReady(player1);
        harness.addToBattlefield(player1, new Millstone());
        Permanent millstone = findPermanent(player1, "Millstone");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        assertThat(millstone.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(millstone.getEffectivePower()).isEqualTo(2);
        assertThat(millstone.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addKarnReady(player1);
        Permanent target = addCreatureReady(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }

    @Test
    @DisplayName("Animation is cleared at end of turn")
    void animationClearedAtEndOfTurn() {
        addKarnReady(player1);
        harness.addToBattlefield(player1, new Millstone());
        Permanent millstone = findPermanent(player1, "Millstone");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(millstone.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, millstone)).isFalse();
    }

    private Permanent addKarnReady(Player player) {
        return addCreatureReady(player, new KarnSilverGolem());
    }

    private Permanent addReadyBears(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
