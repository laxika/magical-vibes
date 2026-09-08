package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CocoonTest extends BaseCardTest {

    @Test
    @DisplayName("Cocoon taps the enchanted creature and enters with three pupa counters")
    void entersTapsCreatureAndAddsPupaCounters() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castCocoon(creature);

        Permanent cocoon = cocoonOnBattlefield(player1);
        Permanent liveCreature = gqs.findPermanentById(gd, creature.getId());
        assertThat(liveCreature.isTapped()).isTrue();
        assertThat(cocoon.getCounterCount(CounterType.PUPA)).isEqualTo(3);
    }

    @Test
    @DisplayName("A pupa counter is removed at upkeep while the enchanted creature stays tapped")
    void removesPupaCounterAndPreventsUntap() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castCocoon(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(cocoonOnBattlefield(player1).getCounterCount(CounterType.PUPA)).isEqualTo(2);
        assertThat(gqs.findPermanentById(gd, creature.getId()).isTapped()).isTrue();
    }

    @Test
    @DisplayName("With no pupa counters, Cocoon is sacrificed and the creature gains a counter and flying")
    void sacrificesAndRewardsEnchantedCreatureWhenCountersRunOut() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castCocoon(creature);

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Cocoon");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    private void castCocoon(Permanent creature) {
        harness.setHand(player1, List.of(new Cocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.inMutationScope(() -> {
            harness.getStackResolutionService().resolveTopOfStack(gd);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });
    }

    private Permanent cocoonOnBattlefield(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Cocoon"))
                .findFirst()
                .orElseThrow();
    }
}
