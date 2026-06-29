package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignalPestTest extends BaseCardTest {

    // ===== Static effect properties =====

    @Test
    @DisplayName("Signal Pest has CanBeBlockedOnlyByFilter static effect for flying or reach")
    void hasCorrectBlockingRestriction() {
        SignalPest card = new SignalPest();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(CanBeBlockedOnlyByFilterEffect.class);
        CanBeBlockedOnlyByFilterEffect effect = (CanBeBlockedOnlyByFilterEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.blockerPredicate()).isInstanceOf(PermanentAnyOfPredicate.class);
        assertThat(effect.allowedBlockersDescription()).isEqualTo("creatures with flying or reach");
    }

    // ===== Blocking restrictions =====

    @Test
    @DisplayName("Signal Pest cannot be blocked by a normal creature")
    void cannotBeBlockedByNormalCreature() {
        Permanent pest = attackingPest();
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or reach");
    }

    @Test
    @DisplayName("Signal Pest can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent pest = attackingPest();
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent flyer = new Permanent(new AvenFisher());
        flyer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(flyer);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Signal Pest can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent pest = attackingPest();
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    // ===== Battle cry =====

    @Test
    @DisplayName("Signal Pest's battle cry triggers when attacking")
    void battleCryTriggersOnAttack() {
        Permanent pest = new Permanent(new SignalPest());
        pest.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Signal Pest");
    }

    @Test
    @DisplayName("Signal Pest's battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent pest = new Permanent(new SignalPest());
        pest.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(3); // 2 base + 1 battle cry
    }

    @Test
    @DisplayName("Signal Pest does not get its own battle cry boost")
    void battleCryDoesNotBoostSelf() {
        Permanent pest = new Permanent(new SignalPest());
        pest.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pest);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));
        harness.passBothPriorities();

        // Signal Pest (0/1) should NOT boost itself
        assertThat(pest.getPowerModifier()).isEqualTo(0);
        assertThat(pest.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent attackingPest() {
        Permanent pest = new Permanent(new SignalPest());
        pest.setSummoningSick(false);
        pest.setAttacking(true);
        return pest;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
