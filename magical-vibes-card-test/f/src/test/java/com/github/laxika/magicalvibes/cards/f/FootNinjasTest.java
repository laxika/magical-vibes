package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FootNinjas.class})
class FootNinjasTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void stopBeforeCombatDamage() {
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS));
    }

    @Test
    @DisplayName("Entering the battlefield gains 3 life")
    void enteringTheBattlefieldGainsLife() {
        harness.setHand(player1, List.of(new FootNinjas()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and enters tapped and attacking")
    void sneakReturnsAnUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new FootNinjas());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new FootNinjas()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Foot Ninjas");
        Permanent footNinjas = findPermanent(player1, "Foot Ninjas");
        assertThat(footNinjas.isTapped()).isTrue();
        assertThat(footNinjas.isAttacking()).isTrue();
        assertThat(footNinjas.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void normalCastWithBlackManaEntersWithoutAttacking() {
        harness.setHand(player1, List.of(new FootNinjas()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent ninjas = findPermanent(player1, "Foot Ninjas");
        assertThat(ninjas.isTapped()).isFalse();
        assertThat(ninjas.isAttacking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void sneakWithBlackManaReturnsAttackerBeforeResolution() {
        Permanent attacker = prepareSneakAttacker();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInHand(player1, "Foot Ninjas");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        resolveAllTriggers();

        Permanent ninjas = findPermanent(player1, "Foot Ninjas");
        assertThat(ninjas.isTapped()).isTrue();
        assertThat(ninjas.isAttacking()).isTrue();
        assertThat(ninjas.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void sneakCannotReturnBlockedAttacker() {
        Permanent attacker = prepareSneakAttacker();
        Permanent blocker = addCreatureReady(player2, new FootNinjas());
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(attacker.getId());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sneakCannotReturnNonattackingCreature() {
        Permanent attacker = prepareSneakAttacker();
        attacker.setAttacking(false);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sneakCannotBeUsedOutsideDeclareBlockers() {
        Permanent attacker = prepareSneakAttacker();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent prepareSneakAttacker() {
        Permanent attacker = addCreatureReady(player1, new FootNinjas());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new FootNinjas()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        return attacker;
    }
}
