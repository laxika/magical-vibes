package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcquiredMutation.class, GrizzlyBears.class})
class AcquiredMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and is goaded")
    void enchantedCreatureGetsBoostAndGoaded() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachMutation(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isOne();
    }

    @Test
    @DisplayName("Attacking enchanted creature gives the defending player two rad counters")
    void attackingEnchantedCreatureGivesDefendingPlayerRadCounters() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMutation(player1, creature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
    }

    private Permanent attachMutation(com.github.laxika.magicalvibes.model.Player controller,
                                     Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new AcquiredMutation());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
