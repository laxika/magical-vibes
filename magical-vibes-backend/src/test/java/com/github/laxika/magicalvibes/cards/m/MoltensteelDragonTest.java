package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoltensteelDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Has activated ability with Phyrexian red mana cost")
    void hasCorrectActivatedAbility() {
        MoltensteelDragon card = new MoltensteelDragon();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{R/P}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent perm = addDragonReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 until end of turn")
    void resolvingGivesPlusOnePlusZero() {
        Permanent perm = addDragonReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(1);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times to stack the bonus")
    void canActivateMultipleTimes() {
        Permanent perm = addDragonReady(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(3);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Bonus resets at end of turn cleanup")
    void bonusResetsAtEndOfTurn() {
        Permanent perm = addDragonReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isEqualTo(0);
        assertThat(perm.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can pay Phyrexian mana with life instead of red mana")
    void canPayPhyrexianWithLife() {
        Permanent perm = addDragonReady(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(perm.getPowerModifier()).isEqualTo(1);
    }

    private Permanent addDragonReady(Player player) {
        Permanent perm = new Permanent(new MoltensteelDragon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
