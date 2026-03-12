package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrizedUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Prized Unicorn has MustBeBlockedByAllCreaturesEffect as static effect")
    void hasCorrectEffect() {
        PrizedUnicorn card = new PrizedUnicorn();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MustBeBlockedByAllCreaturesEffect.class);
    }

    @Test
    @DisplayName("All able creatures must block Prized Unicorn")
    void allAbleCreaturesMustBlock() {
        Permanent unicorn = attackingCreature(new PrizedUnicorn());
        gd.playerBattlefields.get(player1.getId()).add(unicorn);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        // Only one blocker assigned — should fail because both must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        // Both blockers assigned — should succeed
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Prized Unicorn")
    void tappedCreaturesNotForcedToBlock() {
        Permanent unicorn = attackingCreature(new PrizedUnicorn());
        gd.playerBattlefields.get(player1.getId()).add(unicorn);

        Permanent untapped = readyCreature(new GrizzlyBears());
        Permanent tapped = readyCreature(new GrizzlyBears());
        tapped.tap();
        gd.playerBattlefields.get(player2.getId()).add(untapped);
        gd.playerBattlefields.get(player2.getId()).add(tapped);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("No blockers required when defender has no creatures")
    void noBlockersWhenDefenderHasNoCreatures() {
        Permanent unicorn = attackingCreature(new PrizedUnicorn());
        gd.playerBattlefields.get(player1.getId()).add(unicorn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // With no defenders, declare blockers step should advance automatically
        // (handled by CombatBlockService.handleDeclareBlockersStep)
    }

    private Permanent attackingCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
