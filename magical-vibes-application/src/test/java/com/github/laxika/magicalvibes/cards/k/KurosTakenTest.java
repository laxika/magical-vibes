package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GnatMiser;
import com.github.laxika.magicalvibes.cards.h.HandOfCruelty;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KurosTaken.class, GnatMiser.class, HandOfCruelty.class, KitsuneBonesetter.class})
class KurosTakenTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido gives Kuros's Taken +1/+1 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent taken = addCreatureReady(player1, new KurosTaken());
        addCreatureReady(player2, new GnatMiser());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gqs.getEffectivePower(gd, taken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, taken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido gives Kuros's Taken +1/+1 when it blocks")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new GnatMiser());
        Permanent taken = addCreatureReady(player2, new KurosTaken());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gqs.getEffectivePower(gd, taken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, taken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures gives only one Bushido bonus")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent taken = addCreatureReady(player1, new KurosTaken());
        addCreatureReady(player2, new KitsuneBonesetter());
        addCreatureReady(player2, new KitsuneBonesetter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, taken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, taken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent taken = addCreatureReady(player1, new KurosTaken());
        addCreatureReady(player2, new GnatMiser());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gqs.getEffectivePower(gd, taken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, taken)).isEqualTo(2);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, taken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, taken)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating {1}{B} grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent taken = addCreatureReady(player1, new KurosTaken());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(taken.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves Kuros's Taken from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent taken = addCreatureReady(player1, new KurosTaken());
        addCreatureReady(player2, new HandOfCruelty());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(taken);
        assertThat(taken.isTapped()).isTrue();
        assertThat(taken.getMarkedDamage()).isZero();
        assertThat(taken.getRegenerationShield()).isZero();
    }
}
