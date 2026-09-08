package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DwarvenVigilantes;
import com.github.laxika.magicalvibes.cards.j.JungleBasin;
import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

@CardUsed({ResistanceFighter.class, Warthog.class, JungleBasin.class, DwarvenVigilantes.class,
        PantherWarriors.class})
class ResistanceFighterTest extends BaseCardTest {

    @Test
    @DisplayName("Prevented attacker deals no combat damage to the player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        addReadyFighter();
        Permanent attacker = addAttacker(player2, new Warthog());

        activateFighter(attacker);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Fighter is sacrificed as a cost when the ability is activated")
    void sacrificedAsCost() {
        addReadyFighter();
        Permanent attacker = addAttacker(player2, new Warthog());

        activateFighter(attacker);

        harness.assertNotOnBattlefield(player1, "Resistance Fighter");
        harness.assertInGraveyard(player1, "Resistance Fighter");
    }

    @Test
    @DisplayName("Only combat damage is prevented, not all damage")
    void combatDamageOnly() {
        addReadyFighter();
        Permanent attacker = addAttacker(player2, new Warthog());

        activateFighter(attacker);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        addReadyFighter();
        Permanent attacker = addAttacker(player2, new Warthog());

        activateFighter(attacker);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // POSTCOMBAT_MAIN -> END_STEP

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Combat-damage prevention does not prevent noncombat damage from the targeted creature")
    void noncombatDamageStillDeals() {
        addReadyFighter();
        Permanent victim = addCreatureReady(player1, new Warthog());
        Permanent attacker = addAttacker(player2, new DwarvenVigilantes());

        activateFighter(attacker);
        resolveUnblockedCombat();

        harness.handlePermanentChosen(player2, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Prevents combat damage from a targeted blocking creature")
    void preventsCombatDamageFromBlockingCreature() {
        addReadyFighter();
        Permanent blocker = addCreatureReady(player1, new PantherWarriors());
        Permanent attacker = addAttacker(player2, new Warthog());

        activateFighter(blocker);
        prepareDeclareBlockers(player2);
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyFighter();
        UUID landId = harness.addToBattlefieldAndReturn(player1, new JungleBasin()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void addReadyFighter() {
        addCreatureReady(player1, new ResistanceFighter());
    }

    private void activateFighter(Permanent target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private void resolveUnblockedCombat() {
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();
    }
}
