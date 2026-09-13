package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThievesGuildEnforcer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProsperousThief.class, GrizzlyBears.class, ThievesGuildEnforcer.class})
class ProsperousThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Prosperous Thief onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ProsperousThief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent thief = findPermanent(player1, "Prosperous Thief");
        assertThat(thief.isTapped()).isTrue();
        assertThat(thief.isAttacking()).isTrue();
        assertThat(thief.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Creates one Treasure when a Ninja or Rogue deals combat damage")
    void createsTreasureForNinjaOrRogueCombatDamage() {
        addCreatureReady(player1, new ProsperousThief());
        addCreatureReady(player1, new ThievesGuildEnforcer());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates only one Treasure when multiple Ninjas or Rogues deal damage in one combat damage step")
    void batchesMultipleNinjaOrRogueDealers() {
        addCreatureReady(player1, new ProsperousThief());
        addCreatureReady(player1, new ThievesGuildEnforcer());
        addCreatureReady(player1, new ThievesGuildEnforcer());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when a non-Ninja and non-Rogue deals combat damage")
    void doesNotTriggerForOtherCreatureTypes() {
        addCreatureReady(player1, new ProsperousThief());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }
}
