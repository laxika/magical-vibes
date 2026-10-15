package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoarOfJukai.class, FirstVolley.class, Forest.class, GrizzlyBears.class, HolyDay.class})
class RoarOfJukaiTest extends BaseCardTest {

    @Test
    @DisplayName("Gives each blocked creature +2/+2 when you control a Forest")
    void boostsBlockedCreaturesWithForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        Permanent unblocked = addAttackingCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());

        castRoar();

        assertThat(blocked.getEffectivePower()).isEqualTo(4);
        assertThat(blocked.getEffectiveToughness()).isEqualTo(4);
        assertThat(unblocked.getPowerModifier()).isZero();
        assertThat(blocker.getPowerModifier()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not boost blocked creatures without a Forest")
    void doesNotBoostWithoutForest() {
        Permanent blocked = addAttackingCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());

        castRoar();

        assertThat(blocked.getPowerModifier()).isZero();
        assertThat(blocked.getToughnessModifier()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Boosts each blocked creature regardless of its controller")
    void boostsOpponentsBlockedCreature() {
        harness.addToBattlefield(player1, new Forest());
        gd.activePlayerId = player2.getId();
        Permanent player2Blocked = addAttackingCreature(player2);
        Permanent player1Blocker = addReadyCreature(player1);
        player1Blocker.setBlocking(true);
        player1Blocker.addBlockingTargetId(player2Blocked.getId());

        castRoar();

        assertThat(player2Blocked.getEffectivePower()).isEqualTo(4);
        assertThat(player2Blocked.getEffectiveToughness()).isEqualTo(4);
        assertThat(player1Blocker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());

        castRoar();
        assertThat(blocked.getEffectivePower()).isEqualTo(4);
        assertThat(blocked.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(blocked.getPowerModifier()).isZero();
        assertThat(blocked.getToughnessModifier()).isZero();
        assertThat(blocked.getEffectivePower()).isEqualTo(2);
        assertThat(blocked.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Splicing applies Roar of Jukai and makes an opponent gain 5 life")
    void splicesForOpponentLifeGain() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new FirstVolley(), new RoarOfJukai()));
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1));
        harness.assertLife(player2, 15);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(blocked.getEffectivePower()).isEqualTo(4);
        assertThat(blocked.getEffectiveToughness()).isEqualTo(4);
        harness.assertInHand(player1, "Roar of Jukai");
    }

    @Test
    @DisplayName("Splices onto a real Arcane spell and resolves both spells")
    void splicesOntoRealArcaneSpell() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());
        Permanent firstVolleyTarget = addReadyCreature(player2);
        harness.setHand(player1, List.of(new FirstVolley(), new RoarOfJukai()));
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, firstVolleyTarget.getId(), List.of(1));
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(firstVolleyTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(blocked.getEffectivePower()).isEqualTo(4);
        assertThat(blocked.getEffectiveToughness()).isEqualTo(4);
        harness.assertInHand(player1, "Roar of Jukai");
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void rejectsNonArcaneSpliceHost() {
        harness.setHand(player1, List.of(new HolyDay(), new RoarOfJukai()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }

    private void castRoar() {
        harness.castFromHand(player1, new RoarOfJukai(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Splice life gain is paid even without a Forest and before resolution")
    void paysSpliceCostWithoutForest() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new FirstVolley(), new RoarOfJukai()));
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1));

        harness.assertLife(player2, 15);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInHand(player1, "Roar of Jukai");
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's Forest does not satisfy the condition")
    void opponentsForestDoesNotEnableBoost() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        blocked.setBlockedWithoutBlockers(true);

        castRoar();

        assertThat(blocked.getPowerModifier()).isZero();
        assertThat(blocked.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The splice cost remains paid when the host's only target becomes illegal")
    void spliceLifeGainSurvivesIllegalHostTarget() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new FirstVolley(), new RoarOfJukai()));
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithSplice(player1, 0, target.getId(), List.of(1));
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "First Volley");
        harness.assertInHand(player1, "Roar of Jukai");
    }

    @Test
    @DisplayName("A blocked attacker still receives the boost with no remaining blockers")
    void boostsBlockedAttackerWithoutRemainingBlockers() {
        harness.addToBattlefield(player1, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        blocked.setBlockedWithoutBlockers(true);

        castRoar();

        assertThat(blocked.getEffectivePower()).isEqualTo(4);
        assertThat(blocked.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The Forest condition is checked at resolution")
    void losingForestBeforeResolutionPreventsBoost() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent blocked = addAttackingCreature(player1);
        blocked.setBlockedWithoutBlockers(true);
        harness.castFromHand(player1, new RoarOfJukai(), "{2}{G}");
        gd.playerBattlefields.get(player1.getId()).remove(forest);

        harness.passBothPriorities();

        assertThat(blocked.getPowerModifier()).isZero();
        assertThat(blocked.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can resolve with no Forest and no creatures")
    void resolvesOnEmptyBattlefield() {
        castRoar();

        harness.assertInGraveyard(player1, "Roar of Jukai");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addAttackingCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = addReadyCreature(player);
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
