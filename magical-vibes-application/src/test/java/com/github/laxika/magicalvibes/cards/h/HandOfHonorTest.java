package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DeathmaskNezumi;
import com.github.laxika.magicalvibes.cards.g.GodosIrregulars;
import com.github.laxika.magicalvibes.cards.k.KagemarosClutch;
import com.github.laxika.magicalvibes.cards.k.KikusShadow;
import com.github.laxika.magicalvibes.cards.s.SpiralingEmbers;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HandOfHonor.class, GodosIrregulars.class, DeathmaskNezumi.class,
        KagemarosClutch.class, KikusShadow.class, SpiralingEmbers.class})
class HandOfHonorTest extends BaseCardTest {

    @Test
    @DisplayName("When Hand of Honor becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent hand = addCreatureReady(player1, new HandOfHonor());
        hand.setAttacking(true);
        addCreatureReady(player2, new GodosIrregulars());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Hand of Honor blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        attacker.setAttacking(true);
        Permanent hand = addCreatureReady(player2, new HandOfHonor());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(hand.getPowerModifier()).isEqualTo(1);
        assertThat(hand.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Hand of Honor is unblocked, it gets no Bushido bonus")
    void unblockedGetsNoBushidoBonus() {
        Permanent hand = addCreatureReady(player1, new HandOfHonor());
        hand.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(hand.getPowerModifier()).isZero();
        assertThat(hand.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Hand of Honor's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent hand = addCreatureReady(player1, new HandOfHonor());
        hand.setAttacking(true);
        addCreatureReady(player2, new GodosIrregulars());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hand)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hand)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hand of Honor gets only one Bushido bonus when multiple creatures block it")
    void becomesBlockedByMultipleCreaturesGetsOneBushidoBonus() {
        Permanent hand = addCreatureReady(player1, new HandOfHonor());
        hand.setAttacking(true);
        addCreatureReady(player2, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            prepareDeclareBlockers();
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0),
                    new BlockerAssignment(1, 0)));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hand)).isEqualTo(3);
    }

    @Test
    @DisplayName("Black creature cannot block Hand of Honor")
    void blackCreatureCannotBlock() {
        Permanent hand = addCreatureReady(player1, new HandOfHonor());
        hand.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DeathmaskNezumi());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Black creature deals no combat damage to Hand of Honor")
    void takesNoDamageFromBlackCreature() {
        Permanent attacker = addCreatureReady(player1, new DeathmaskNezumi());
        attacker.setAttacking(true);
        Permanent hand = addCreatureReady(player2, new HandOfHonor());
        hand.setBlocking(true);
        hand.addBlockingTarget(0);

        prepareDeclareBlockers();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hand);
        assertThat(hand.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Black sorcery cannot target Hand of Honor")
    void cannotBeTargetedByBlackSorcery() {
        Permanent hand = addCreatureReady(player2, new HandOfHonor());

        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, hand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Red sorcery can target Hand of Honor")
    void canBeTargetedByRedSorcery() {
        Permanent hand = addCreatureReady(player1, new HandOfHonor());

        harness.setHand(player1, List.of(new SpiralingEmbers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, hand.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Spiraling Embers");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(hand.getId());
    }

    @Test
    @DisplayName("Black Aura cannot enchant Hand of Honor")
    void cannotBeEnchantedByBlackAura() {
        Permanent hand = addCreatureReady(player2, new HandOfHonor());

        harness.setHand(player1, List.of(new KagemarosClutch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, hand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }
}
