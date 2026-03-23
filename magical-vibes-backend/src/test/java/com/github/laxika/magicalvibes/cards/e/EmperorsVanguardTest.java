package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmperorsVanguardTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_COMBAT_DAMAGE_TO_PLAYER ExploreEffect")
    void hasCorrectEffect() {
        EmperorsVanguard card = new EmperorsVanguard();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(ExploreEffect.class);
    }

    // ===== Explore on combat damage to player — land on top =====

    @Test
    @DisplayName("Deals combat damage and explores with land on top — land goes to hand")
    void exploreLandGoesToHand() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        resolveCombatAndExploreTrigger();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Deals combat damage and explores with land on top — no +1/+1 counter")
    void exploreLandNoCounter() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        resolveCombatAndExploreTrigger();

        assertThat(vanguard.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== Explore on combat damage to player — non-land on top =====

    @Test
    @DisplayName("Deals combat damage and explores with non-land on top — gets +1/+1 counter")
    void exploreNonLandAddsCounter() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        resolveCombatAndExploreTrigger();

        assertThat(vanguard.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals combat damage and explores with non-land — accept puts card into graveyard")
    void exploreNonLandAcceptPutsInGraveyard() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        resolveCombatAndExploreTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Deals combat damage and explores with non-land — decline leaves card on top")
    void exploreNonLandDeclineLeavesOnTop() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        resolveCombatAndExploreTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId())
                .isEqualTo(creature.getId());
    }

    // ===== No trigger when blocked =====

    @Test
    @DisplayName("No explore trigger when blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);

        // 4/4 blocker — Vanguard is 4/3, both die
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        resolveCombat();

        // Vanguard should be dead — no explore trigger
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Emperor's Vanguard"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    // ===== Combat damage dealt =====

    @Test
    @DisplayName("Deals 4 combat damage to defending player")
    void dealsCombatDamage() {
        Permanent vanguard = addVanguardReady(player1);
        vanguard.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Helpers =====

    private Permanent addVanguardReady(Player player) {
        Permanent perm = new Permanent(new EmperorsVanguard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombatAndExploreTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage dealt, explore trigger put on stack
        harness.passBothPriorities(); // resolve explore trigger
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
