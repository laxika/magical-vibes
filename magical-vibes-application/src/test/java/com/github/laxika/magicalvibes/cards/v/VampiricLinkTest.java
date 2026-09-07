package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CinderPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampiricLink.class, GrizzlyBears.class, CinderPyromancer.class})
class VampiricLinkTest extends BaseCardTest {

    @Test
    @DisplayName("You gain life equal to combat damage dealt by the enchanted creature")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        GrizzlyBears card = new GrizzlyBears();
        card.setPower(3);
        Permanent creature = addCreatureReady(player2, card);
        attachVampiricLink(creature);
        creature.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("You gain life equal to noncombat damage dealt by the enchanted creature")
    void gainsLifeFromNoncombatDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player2, new CinderPyromancer());
        attachVampiricLink(creature);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void attachVampiricLink(Permanent creature) {
        harness.setHand(player1, List.of(new VampiricLink()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
