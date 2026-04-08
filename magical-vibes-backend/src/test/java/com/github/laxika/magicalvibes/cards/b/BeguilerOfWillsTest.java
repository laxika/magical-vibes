package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostControlledCreatureCountPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeguilerOfWillsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Beguiler of Wills has correct activated ability")
    void hasCorrectActivatedAbility() {
        BeguilerOfWills card = new BeguilerOfWills();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GainControlOfTargetPermanentEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter()).isNotNull();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BeguilerOfWills()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Beguiler of Wills");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BeguilerOfWills()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beguiler of Wills"));
    }

    // ===== Steal: power <= creature count =====

    @Test
    @DisplayName("Steals creature with power equal to number of controlled creatures")
    void stealsCreatureWithPowerEqualToCreatureCount() {
        // Beguiler + GrizzlyBears = 2 creatures; target GrizzlyBears (power 2) on opponent side
        Permanent beguiler = addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Steals creature with power less than number of controlled creatures")
    void stealsCreatureWithPowerLessThanCreatureCount() {
        // Beguiler + GrizzlyBears = 2 creatures; target LlanowarElves (power 1)
        addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Can steal own creature")
    void canStealOwnCreature() {
        // Beguiler + target bears = 2 creatures; bears has power 2
        addReadyBeguiler(player1);
        Permanent target = addReadyCreature(player1, new GrizzlyBears());

        // Targeting own creature is legal
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Should still be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    // ===== Cannot target creature with too much power =====

    @Test
    @DisplayName("Cannot target creature with power greater than number of controlled creatures")
    void cannotTargetCreatureWithTooMuchPower() {
        // Beguiler alone = 1 creature; cannot target GrizzlyBears (power 2)
        addReadyBeguiler(player1);
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Adding more creatures enables targeting higher-power creatures")
    void addingCreaturesEnablesHigherPowerTargets() {
        // Start with Beguiler + 2 GrizzlyBears = 3 creatures; can target HillGiant (power 3)
        addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());

        // Add a non-creature permanent (artifact/enchantment)
        Permanent nonCreature = new Permanent(new com.github.laxika.magicalvibes.cards.p.Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Tap requirement =====

    @Test
    @DisplayName("Activating ability taps Beguiler of Wills")
    void activatingTapsBeguiler() {
        Permanent beguiler = addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(beguiler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when summoning sick")
    void cannotActivateWhenSummoningSick() {
        BeguilerOfWills card = new BeguilerOfWills();
        Permanent beguiler = new Permanent(card);
        beguiler.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(beguiler);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent beguiler = addReadyBeguiler(player1);
        beguiler.tap();
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Stolen creature tracking =====

    @Test
    @DisplayName("Stolen creature is tracked in stolenCreatures map")
    void stolenCreatureIsTracked() {
        addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stolenCreatures).containsEntry(target.getId(), player2.getId());
    }

    @Test
    @DisplayName("Stolen creature has summoning sickness")
    void stolenCreatureHasSummoningSickness() {
        addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isSummoningSick()).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Stack =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Beguiler of Wills");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    // ===== Game log =====

    @Test
    @DisplayName("Stealing adds to game log")
    void stealingAddsToGameLog() {
        addReadyBeguiler(player1);
        addReadyCreature(player1, new GrizzlyBears());
        Permanent target = addReadyCreature(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("gains control of") && log.contains("Llanowar Elves"));
    }

    // ===== Helpers =====

    private Permanent addReadyBeguiler(Player player) {
        BeguilerOfWills card = new BeguilerOfWills();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
