package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CowedByWisdom;
import com.github.laxika.magicalvibes.cards.g.GodosIrregulars;
import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.cards.k.KiyomaroFirstToStand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HandOfCruelty.class, InnerChamberGuard.class, KiyomaroFirstToStand.class,
        GodosIrregulars.class, CowedByWisdom.class})
class HandOfCrueltyTest extends BaseCardTest {

    @Test
    @DisplayName("A white creature cannot block Hand of Cruelty")
    void whiteCreatureCannotBlock() {
        Permanent hand = addHandReady(player1);
        Permanent blocker = addCreatureReady(player2, new InnerChamberGuard());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(hand)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Blocking gives Hand of Cruelty +1/+1 until end of turn")
    void blockingTriggersBushido() {
        Permanent hand = addHandReady(player2);
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(hand),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked gives Hand of Cruelty +1/+1 until end of turn")
    void becomingBlockedTriggersBushido() {
        Permanent hand = addHandReady(player1);
        Permanent blocker = addCreatureReady(player2, new GodosIrregulars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(hand))));
        resolveCombat();

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonwhite creature can block Hand of Cruelty")
    void nonwhiteCreatureCanBlock() {
        Permanent hand = addHandReady(player1);
        Permanent blocker = addCreatureReady(player2, new GodosIrregulars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(hand))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Protection from white prevents combat damage from a white creature")
    void whiteCombatDamageIsPrevented() {
        Permanent hand = addHandReady(player2);
        Permanent attacker = addCreatureReady(player1, new KiyomaroFirstToStand());
        harness.setHand(player1, List.of(
                new HandOfCruelty(), new HandOfCruelty(), new HandOfCruelty(), new HandOfCruelty()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(hand),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(hand.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A white Aura cannot enchant Hand of Cruelty")
    void whiteAuraCannotEnchant() {
        Permanent hand = addHandReady(player2);

        harness.setHand(player1, List.of(new CowedByWisdom()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, hand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Hand of Cruelty's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent hand = addHandReady(player2);
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(hand),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hand)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hand)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple blockers give Hand of Cruelty only one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent hand = addHandReady(player1);
        Permanent firstBlocker = addCreatureReady(player2, new GodosIrregulars());
        Permanent secondBlocker = addCreatureReady(player2, new GodosIrregulars());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackersAndPrepareBlockers(List.of(0));
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                            gd.playerBattlefields.get(player1.getId()).indexOf(hand)),
                    new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                            gd.playerBattlefields.get(player1.getId()).indexOf(hand))));
            resolveAllTriggers();
        });

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    private Permanent addHandReady(Player player) {
        return addCreatureReady(player, new HandOfCruelty());
    }
}
