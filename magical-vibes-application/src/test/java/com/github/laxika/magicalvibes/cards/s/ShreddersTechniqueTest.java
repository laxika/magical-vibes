package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VernalEquinox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShreddersTechnique.class, Forest.class, GrizzlyBears.class, VernalEquinox.class})
class ShreddersTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature without making the spell's controller lose life")
    void destroysCreatureWithoutLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castNormally(target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroys an enchantment and the spell's controller loses 2 life")
    void destroysEnchantmentAndLosesLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernalEquinox());
        castNormally(target.getId());

        harness.assertInGraveyard(player2, "Vernal Equinox");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void canDestroyOwnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VernalEquinox());

        castNormally(target.getId());

        harness.assertInGraveyard(player1, "Vernal Equinox");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void indestructibleEnchantmentDoesNotCauseLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernalEquinox());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castNormally(target.getId());

        harness.assertOnBattlefield(player2, "Vernal Equinox");
        harness.assertLife(player1, 20);
    }

    @Test
    void regeneratedEnchantmentDoesNotCauseLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernalEquinox());
        target.setRegenerationShield(1);

        castNormally(target.getId());

        harness.assertOnBattlefield(player2, "Vernal Equinox");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void enchantmentLeavingBeforeResolutionDoesNotCauseLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernalEquinox());
        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Shredder's Technique");
    }

    @Test
    void cannotUseSneakDuringMainPhase() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotReturnBlockedAttackerForSneak() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        attacker.setBlockedThisCombat(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setBlocking(true);
        target.getBlockingTargetIds().add(attacker.getId());
        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotPayNormalCostDuringDeclareBlockers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and casts for {B}")
    void sneaksAndDestroysCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(attacker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNormally(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
