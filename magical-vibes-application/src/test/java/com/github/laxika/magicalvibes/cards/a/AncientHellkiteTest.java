package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AncientHellkiteTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability with DealDamageToTargetCreatureEffect(1) and ONLY_WHILE_ATTACKING restriction")
    void hasCorrectStructure() {
        AncientHellkite card = new AncientHellkite();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{R}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.ONLY_WHILE_ATTACKING);
        assertThat(ability.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        DealDamageToTargetCreatureEffect effect = (DealDamageToTargetCreatureEffect) ability.getEffects().getFirst();
        assertThat(effect.damage()).isEqualTo(1);
    }

    // ===== Ability resolves while attacking =====

    @Test
    @DisplayName("Deals 1 damage to target creature defending player controls while attacking — kills 1/1")
    void dealsOneDamageWhileAttacking() {
        Permanent hellkite = addCreatureReady(player1, new AncientHellkite());
        addCreatureReady(player2, new LlanowarElves());

        setUpAttacking(hellkite);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Llanowar Elves is 1/1, 1 damage kills it
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can activate multiple times per turn — no per-turn activation limit")
    void canActivateMultipleTimes() {
        Permanent hellkite = addCreatureReady(player1, new AncientHellkite());
        addCreatureReady(player2, new LlanowarElves());
        addCreatureReady(player2, new GrizzlyBears());

        setUpAttacking(hellkite);

        // Stack two activations targeting different creatures
        UUID target1 = harness.getPermanentId(player2, "Llanowar Elves");
        UUID target2 = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, target1);
        harness.activateAbility(player1, 0, null, target2);
        assertThat(gd.stack).hasSize(2);
    }

    // ===== Cannot activate when not attacking =====

    @Test
    @DisplayName("Cannot activate ability when not attacking")
    void cannotActivateWhenNotAttacking() {
        addCreatureReady(player1, new AncientHellkite());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking");
    }

    // ===== Cannot target own creatures =====

    @Test
    @DisplayName("Cannot target own creatures")
    void cannotTargetOwnCreatures() {
        Permanent hellkite = addCreatureReady(player1, new AncientHellkite());
        addCreatureReady(player1, new GrizzlyBears());

        setUpAttacking(hellkite);

        UUID ownTargetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownTargetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helper methods =====


    private void setUpAttacking(Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        attacker.setAttacking(true);
    }
}
