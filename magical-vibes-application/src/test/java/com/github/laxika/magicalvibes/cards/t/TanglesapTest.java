package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tanglesap.class, GrizzlyBears.class, AvatarOfMight.class})
class TanglesapTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from creatures without trample")
    void preventsCombatDamageFromCreaturesWithoutTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTanglesap();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isTrue();
    }

    @Test
    @DisplayName("Does not prevent combat damage from creatures with trample")
    void allowsCombatDamageFromCreaturesWithTrample() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        castTanglesap();

        assertThat(gqs.isPreventedFromDealingDamage(gd, avatar, true)).isFalse();
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from creatures without trample")
    void doesNotPreventNoncombatDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTanglesap();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, false)).isFalse();
    }

    @Test
    @DisplayName("Trample damage to the blocker and defending player is not prevented")
    void trampleDamageReachesBlockerAndPlayer() {
        Permanent attacker = addCreatureReady(player1, new AvatarOfMight());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        castTanglesap();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 6));

        harness.assertLife(player2, 14);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Prevents attacking damage but allows a blocking creature with trample to deal damage")
    void trampleOnBlockerAlsoExemptsItsDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new AvatarOfMight());

        castTanglesap();
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents damage from creatures entering after resolution")
    void affectsCreaturesEnteringAfterResolution() {
        castTanglesap();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTanglesap();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, bears, true)).isFalse();
    }

    private void castTanglesap() {
        harness.setHand(player1, List.of(new Tanglesap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
    }
}
