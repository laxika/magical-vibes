package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldSelfReturn;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhoulishImpetus.class, GrizzlyBears.class, Forest.class})
class GhoulishImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1, deathtouch, and goad")
    void enchantedCreatureGetsAbilities() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGhoulishImpetus(bear);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, bear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GhoulishImpetus()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Schedules a return at the next end step when the enchanted creature dies")
    void returnsAtNextEndStep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGhoulishImpetus(bear);

        bear.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghoulish Impetus");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldSelfReturn.class)).hasSize(1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldSelfReturn.class)).isEmpty();
        harness.assertInGraveyard(player1, "Ghoulish Impetus");
    }

    private void castGhoulishImpetus(Permanent creature) {
        harness.setHand(player1, List.of(new GhoulishImpetus()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
