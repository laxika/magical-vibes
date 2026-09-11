package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishLyrist;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Redeem.class, ElvishLyrist.class, HeatRay.class, Mountain.class})
class RedeemTest extends BaseCardTest {

    private void castRedeem(List<UUID> targets) {
        harness.setHand(player1, List.of(new Redeem()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targets);
    }

    private void heatRay(Permanent target) {
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Prevents all damage to both target creatures this turn")
    void protectsTwoCreatures() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new ElvishLyrist());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new ElvishLyrist());

        castRedeem(List.of(a.getId(), b.getId()));

        heatRay(a);
        heatRay(b);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactlyInAnyOrder(a.getId(), b.getId());
    }

    @Test
    @DisplayName("May target only one creature (up to two)")
    void protectsOneCreature() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new ElvishLyrist());

        castRedeem(List.of(a.getId()));
        heatRay(a);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(a.getId());
    }

    @Test
    @DisplayName("Prevention wears off after turn cleanup")
    void wearsOff() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new ElvishLyrist());

        castRedeem(List.of(a.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        heatRay(a);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(a.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Redeem()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature controlled by another player")
    void protectsOpponentCreature() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new ElvishLyrist());
        Permanent unprotectedCreature = harness.addToBattlefieldAndReturn(player2, new ElvishLyrist());

        castRedeem(List.of(protectedCreature.getId()));
        heatRay(protectedCreature);
        heatRay(unprotectedCreature);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(protectedCreature.getId())
                .doesNotContain(unprotectedCreature.getId());
    }

    @Test
    @DisplayName("May choose no creatures")
    void protectsNoCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishLyrist());

        castRedeem(List.of());
        heatRay(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Prevents combat damage to a target creature")
    void preventsCombatDamageToTarget() {
        Permanent attacker = addCreatureReady(player1, new ElvishLyrist());
        Permanent target = addCreatureReady(player2, new ElvishLyrist());

        castRedeem(List.of(target.getId()));

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(target);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .doesNotContain(attacker.getId());
    }
}
