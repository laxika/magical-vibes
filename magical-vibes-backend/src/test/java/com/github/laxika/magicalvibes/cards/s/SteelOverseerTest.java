package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteelOverseerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Steel Overseer has tap ability with PutPlusOnePlusOneCounterOnEachControlledPermanentEffect")
    void hasCorrectProperties() {
        SteelOverseer card = new SteelOverseer();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(PutPlusOnePlusOneCounterOnEachControlledPermanentEffect.class);
    }

    // ===== Resolving ability =====

    @Test
    @DisplayName("Resolving ability puts a +1/+1 counter on each artifact creature you control")
    void putsCountersOnArtifactCreatures() {
        Permanent overseer = addReadyOverseer(player1);
        Permanent ornithopter = addReadyArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Both Steel Overseer (artifact creature) and Ornithopter (artifact creature) get counters
        assertThat(overseer.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(ornithopter.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put counters on non-artifact creatures")
    void doesNotAffectNonArtifactCreatures() {
        addReadyOverseer(player1);
        Permanent bear = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not put counters on opponent's artifact creatures")
    void doesNotAffectOpponentArtifactCreatures() {
        addReadyOverseer(player1);
        Permanent opponentArtifact = addReadyArtifactCreature(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(opponentArtifact.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    // ===== Tap cost =====

    @Test
    @DisplayName("Activating ability taps Steel Overseer")
    void activatingTapsOverseer() {
        Permanent overseer = addReadyOverseer(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(overseer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent overseer = new Permanent(new SteelOverseer());
        overseer.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(overseer);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Counters accumulate =====

    @Test
    @DisplayName("Counters accumulate across multiple activations")
    void countersAccumulate() {
        Permanent overseer = addReadyOverseer(player1);
        Permanent ornithopter = addReadyArtifactCreature(player1);

        // First activation
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ornithopter.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Untap overseer for second activation
        overseer.setTapped(false);

        // Second activation
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ornithopter.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== Stack behavior =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void putsAbilityOnStack() {
        addReadyOverseer(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Steel Overseer");
    }

    // ===== Helpers =====

    private Permanent addReadyOverseer(Player player) {
        Permanent perm = new Permanent(new SteelOverseer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifactCreature(Player player) {
        Permanent perm = new Permanent(new Ornithopter());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
