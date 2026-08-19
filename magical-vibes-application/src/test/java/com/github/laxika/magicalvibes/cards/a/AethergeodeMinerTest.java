package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AethergeodeMinerTest extends BaseCardTest {

    @Test
    void getsTwoEnergyCountersWhenItAttacks() {
        addCreatureReady(player1, new AethergeodeMiner());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysTwoEnergyToExileAndReturnIt() {
        Permanent miner = addCreatureReady(player1, new AethergeodeMiner());
        UUID oldId = miner.getId();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Aethergeode Miner");
        assertThat(returned.getId()).isNotEqualTo(oldId);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void cannotActivateWithoutTwoEnergyCounters() {
        addCreatureReady(player1, new AethergeodeMiner());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
