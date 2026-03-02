package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HellkiteIgniterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Hellkite Igniter has correct activated ability")
    void hasCorrectActivatedAbility() {
        HellkiteIgniter card = new HellkiteIgniter();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{R}");
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(BoostSelfPerControlledPermanentEffect.class);

        BoostSelfPerControlledPermanentEffect effect =
                (BoostSelfPerControlledPermanentEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.powerPerPermanent()).isEqualTo(1);
        assertThat(effect.toughnessPerPermanent()).isEqualTo(0);
        assertThat(effect.filter()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Hellkite Igniter");
        assertThat(entry.getSourcePermanentId()).isEqualTo(hellkite.getId());
    }

    @Test
    @DisplayName("Activating ability does not tap Hellkite Igniter")
    void activatingAbilityDoesNotTap() {
        Permanent hellkite = addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(hellkite.isTapped()).isFalse();
    }

    // ===== Boost with artifacts =====

    @Test
    @DisplayName("Gets +0/+0 with no artifacts controlled")
    void getsNoBonusWithNoArtifacts() {
        addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(5);
        assertThat(hellkite.getEffectiveToughness()).isEqualTo(5);
        assertThat(hellkite.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +1/+0 with one artifact controlled")
    void getsPlus1With1Artifact() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(6);
        assertThat(hellkite.getEffectiveToughness()).isEqualTo(5);
        assertThat(hellkite.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +3/+0 with three artifacts controlled")
    void getsPlus3With3Artifacts() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(8);
        assertThat(hellkite.getEffectiveToughness()).isEqualTo(5);
        assertThat(hellkite.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count opponent's artifacts")
    void doesNotCountOpponentArtifacts() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(5);
        assertThat(hellkite.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability multiple times stacks the bonus")
    void multipleActivationsStack() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // +2 from first activation, +2 from second activation = +4 total
        assertThat(hellkite.getEffectivePower()).isEqualTo(9);
        assertThat(hellkite.getPowerModifier()).isEqualTo(4);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent hellkite = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hellkite.getEffectivePower()).isEqualTo(7);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hellkite.getPowerModifier()).isEqualTo(0);
        assertThat(hellkite.getEffectivePower()).isEqualTo(5);
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyHellkite(player1);
        // Only add 1 mana, need {1}{R}
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyHellkite(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Hellkite Igniter is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Hellkite Igniter before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).removeFirst();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Logging =====

    @Test
    @DisplayName("Resolving ability logs the boost")
    void resolvingAbilityLogsBoost() {
        addReadyHellkite(player1);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gets +1/+0"));
    }

    // ===== Helper methods =====

    private Permanent addReadyHellkite(Player player) {
        HellkiteIgniter card = new HellkiteIgniter();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).addFirst(perm);
        return perm;
    }
}
