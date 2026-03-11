package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.cards.g.GoblinTunneler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinTunnelerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Goblin Tunneler has one tap activated ability targeting creature with power 2 or less")
    void hasUnblockableActivatedAbility() {
        GoblinTunneler card = new GoblinTunneler();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(MakeCreatureUnblockableEffect.class);
        MakeCreatureUnblockableEffect effect = (MakeCreatureUnblockableEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.selfTargeting()).isFalse();
    }

    // ===== Activated ability: make target creature unblockable =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingAbilityPutsOnStack() {
        Permanent tunneler = addTunnelerReady(player1);
        Permanent target = addSmallCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Goblin Tunneler");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability makes target creature unblockable this turn")
    void resolvingAbilityMakesTargetUnblockable() {
        Permanent tunneler = addTunnelerReady(player1);
        Permanent target = addSmallCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Tunneler itself is not made unblockable when targeting another creature")
    void tunnelerNotUnblockable() {
        Permanent tunneler = addTunnelerReady(player1);
        Permanent target = addSmallCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(tunneler.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockable resets at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent tunneler = addTunnelerReady(player1);
        Permanent target = addSmallCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Can target opponent's creature with power 2 or less")
    void canTargetOpponentCreature() {
        Permanent tunneler = addTunnelerReady(player1);
        Permanent opponentCreature = addSmallCreature(player2);

        harness.activateAbility(player1, 0, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBeBlocked()).isTrue();
    }

    // ===== Tap cost constraints =====

    @Test
    @DisplayName("Activating ability taps Goblin Tunneler")
    void activatingAbilityTapsTunneler() {
        Permanent tunneler = addTunnelerReady(player1);
        Permanent target = addSmallCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(tunneler.isTapped()).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        Permanent tunneler = addTunnelerReady(player1);
        Permanent target = addSmallCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addTunnelerReady(Player player) {
        Permanent perm = new Permanent(new GoblinTunneler());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSmallCreature(Player player) {
        Permanent perm = new Permanent(new GoblinTunneler());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
