package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleThisTurnEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmergentGrowthTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Emergent Growth has correct effects")
    void hasCorrectEffects() {
        EmergentGrowth card = new EmergentGrowth();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(boost.powerBoost()).isEqualTo(5);
        assertThat(boost.toughnessBoost()).isEqualTo(5);

        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(MustBeBlockedIfAbleThisTurnEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Emergent Growth gives +5/+5 and sets must-be-blocked flag")
    void resolvingBoostsAndSetsMustBeBlocked() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(5);
        assertThat(bears.getToughnessModifier()).isEqualTo(5);
        assertThat(bears.getEffectivePower()).isEqualTo(7);
        assertThat(bears.getEffectiveToughness()).isEqualTo(7);
        assertThat(bears.isMustBeBlockedThisTurn()).isTrue();
    }

    // ===== Combat interaction =====

    @Test
    @DisplayName("Creature with must-be-blocked flag must be blocked if able")
    void mustBeBlockedIfAble() {
        Permanent attacker = attackingCreature(new GrizzlyBears());
        attacker.setMustBeBlockedThisTurn(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("One blocker satisfies the must-be-blocked requirement")
    void oneBlockerSuffices() {
        Permanent attacker = attackingCreature(new GrizzlyBears());
        attacker.setMustBeBlockedThisTurn(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block")
    void tappedCreaturesNotForcedToBlock() {
        Permanent attacker = attackingCreature(new GrizzlyBears());
        attacker.setMustBeBlockedThisTurn(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent tapped = readyCreature(new GrizzlyBears());
        tapped.tap();
        gd.playerBattlefields.get(player2.getId()).add(tapped);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost and must-be-blocked flag wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.isMustBeBlockedThisTurn()).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Emergent Growth fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmergentGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Emergent Growth"));
    }

    private Permanent attackingCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(Card card) {
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
