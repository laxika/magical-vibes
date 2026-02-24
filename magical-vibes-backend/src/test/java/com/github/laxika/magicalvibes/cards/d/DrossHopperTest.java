package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrossHopperTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has sacrifice-a-creature activated ability that grants flying")
    void hasCorrectAbilityStructure() {
        DrossHopper card = new DrossHopper();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GrantKeywordEffect.class);
    }

    // ===== Activation: sacrifice a creature to gain flying =====

    @Test
    @DisplayName("Sacrificing a creature grants Dross Hopper flying until end of turn")
    void sacrificeCreatureGrantsFlying() {
        Permanent hopper = addReadyHopper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Hopper should have flying
        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Flying granted by ability resets at end of turn")
    void flyingResetsAtEndOfTurn() {
        Permanent hopper = addReadyHopper(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).contains(Keyword.FLYING);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hopper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Can sacrifice Dross Hopper to its own ability (fizzles on resolution)")
    void canSacrificeItself() {
        addReadyHopper(player1);
        UUID hopperId = harness.getPermanentId(player1, "Dross Hopper");

        harness.activateAbility(player1, 0, null, hopperId);

        // Hopper should be sacrificed, ability on stack
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dross Hopper"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        // Resolve — fizzles since hopper is gone
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability has no mana cost and does not require tap")
    void noManaCostNoTapRequired() {
        Permanent hopper = addReadyHopper(player1);
        hopper.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        // No mana added, hopper is tapped — should still work
        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutSacrificeTarget() {
        addReadyHopper(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyHopper(Player player) {
        DrossHopper card = new DrossHopper();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
