package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResistanceFighterTest extends BaseCardTest {

    @Test
    @DisplayName("Prevented attacker deals no combat damage to the player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        addReadyFighter();
        Permanent attacker = addAttacker(player2, new GrizzlyBears());

        activateFighter(attacker);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Fighter is sacrificed as a cost when the ability is activated")
    void sacrificedAsCost() {
        addReadyFighter();
        Permanent attacker = addAttacker(player2, new GrizzlyBears());

        activateFighter(attacker);

        harness.assertNotOnBattlefield(player1, "Resistance Fighter");
        harness.assertInGraveyard(player1, "Resistance Fighter");
    }

    @Test
    @DisplayName("Only combat damage is prevented, not all damage")
    void combatDamageOnly() {
        addReadyFighter();
        Permanent attacker = addAttacker(player2, new GrizzlyBears());

        activateFighter(attacker);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        addReadyFighter();
        Permanent attacker = addAttacker(player2, new GrizzlyBears());

        activateFighter(attacker);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // POSTCOMBAT_MAIN -> END_STEP

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyFighter();
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void addReadyFighter() {
        harness.addToBattlefield(player1, new ResistanceFighter());
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Resistance Fighter"))
                .forEach(p -> p.setSummoningSick(false));
    }

    private void activateFighter(Permanent target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(owner, card);
        Permanent attacker = gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(card.getName()))
                .findFirst().orElseThrow();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private void resolveCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
