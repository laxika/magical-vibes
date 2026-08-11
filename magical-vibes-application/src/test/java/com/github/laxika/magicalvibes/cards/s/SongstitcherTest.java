package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SongstitcherTest extends BaseCardTest {

    @Test
    @DisplayName("Ability prevents combat damage from an attacking creature with flying")
    void preventsCombatDamageFromFlyingAttacker() {
        addCreatureReady(player1, new Songstitcher());
        Permanent attacker = addAttacker(player2, new SuntailHawk());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target an attacking creature without flying")
    void cannotTargetNonFlyingAttacker() {
        addCreatureReady(player1, new Songstitcher());
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature with flying");
    }

    @Test
    @DisplayName("Cannot target a flying creature that is not attacking")
    void cannotTargetNonAttackingFlyer() {
        addCreatureReady(player1, new Songstitcher());
        Permanent flyer = addCreatureReady(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature with flying");
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        addCreatureReady(player1, new Songstitcher());
        Permanent attacker = addAttacker(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.permanentsPreventedFromDealingDamage).isEmpty();
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
