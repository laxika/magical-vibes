package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpitefulReturnedTest extends BaseCardTest {

    @Test
    @DisplayName("Spiteful Returned makes the defending player lose 2 life when it attacks")
    void creatureAttackMakesDefendingPlayerLoseLife() {
        addCreatureReady(player1, new SpitefulReturned());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and triggers when it attacks")
    void bestowBoostsAndTriggersWhenEnchantedCreatureAttacks() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpitefulReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
