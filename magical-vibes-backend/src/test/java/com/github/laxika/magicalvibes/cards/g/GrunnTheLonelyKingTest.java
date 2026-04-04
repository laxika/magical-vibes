package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AttacksAloneConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersIfKickedEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrunnTheLonelyKingTest extends BaseCardTest {

    // ===== Card setup =====

    @Test
    @DisplayName("Has KickerEffect with cost {3}")
    void hasKickerEffect() {
        GrunnTheLonelyKing card = new GrunnTheLonelyKing();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof KickerEffect ke && ke.cost().equals("{3}"));
    }

    @Test
    @DisplayName("Has EnterWithPlusOnePlusOneCountersIfKickedEffect with count 5")
    void hasKickedETBEffect() {
        GrunnTheLonelyKing card = new GrunnTheLonelyKing();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithPlusOnePlusOneCountersIfKickedEffect.class);
        var effect = (EnterWithPlusOnePlusOneCountersIfKickedEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("Has ON_ATTACK trigger with AttacksAloneConditionalEffect wrapping DoubleSelfPowerToughnessEffect")
    void hasAttacksAloneTrigger() {
        GrunnTheLonelyKing card = new GrunnTheLonelyKing();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(AttacksAloneConditionalEffect.class);
        var conditional = (AttacksAloneConditionalEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(DoubleSelfPowerToughnessEffect.class);
    }

    // ===== Casting without kicker =====

    @Test
    @DisplayName("Cast without kicker — enters as 5/5 with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new GrunnTheLonelyKing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 4); // 4 generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent grunn = findGrunn(player1);
        assertThat(grunn).isNotNull();
        assertThat(grunn.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== Casting with kicker =====

    @Test
    @DisplayName("Cast with kicker — enters with five +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new GrunnTheLonelyKing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 7); // 4 generic + 3 kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent grunn = findGrunn(player1);
        assertThat(grunn).isNotNull();
        assertThat(grunn.getPlusOnePlusOneCounters()).isEqualTo(5);
    }

    // ===== Attacks alone — trigger fires =====

    @Test
    @DisplayName("Attacking alone puts trigger on the stack")
    void attackingAlonePutsTriggerOnStack() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grunn, the Lonely King");
    }

    @Test
    @DisplayName("Attacking alone — non-kicked 5/5 becomes 10/10 until end of turn")
    void attackingAloneDoublesPowerToughness() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        int power = gqs.getEffectivePower(gd, grunn);
        int toughness = gqs.getEffectiveToughness(gd, grunn);
        assertThat(power).isEqualTo(10);
        assertThat(toughness).isEqualTo(10);
    }

    @Test
    @DisplayName("Attacking alone — kicked 10/10 becomes 20/20 until end of turn")
    void attackingAloneKickedDoubles() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());
        grunn.setPlusOnePlusOneCounters(5); // simulate kicked ETB

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        int power = gqs.getEffectivePower(gd, grunn);
        int toughness = gqs.getEffectiveToughness(gd, grunn);
        assertThat(power).isEqualTo(20);
        assertThat(toughness).isEqualTo(20);
    }

    // ===== Not attacking alone — trigger does not fire =====

    @Test
    @DisplayName("Attacking with another creature — trigger does not fire")
    void attackingWithOtherCreatureNoTrigger() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        // No attacks-alone trigger should be on the stack
        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Grunn, the Lonely King"));
    }

    @Test
    @DisplayName("Attacking with another creature — power/toughness remain base values")
    void attackingWithOtherCreatureNoPTChange() {
        Permanent grunn = addCreatureReady(player1, new GrunnTheLonelyKing());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        int power = gqs.getEffectivePower(gd, grunn);
        int toughness = gqs.getEffectiveToughness(gd, grunn);
        assertThat(power).isEqualTo(5);
        assertThat(toughness).isEqualTo(5);
    }

    // ===== Helper methods =====


    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private Permanent findGrunn(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grunn, the Lonely King"))
                .findFirst().orElse(null);
    }
}
