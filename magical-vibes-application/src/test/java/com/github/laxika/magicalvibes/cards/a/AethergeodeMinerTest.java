package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AethergeodeMiner.class})
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

    @Test
    void paysEnergyBeforeResolutionAndReturnsUntappedAsANewCreature() {
        Permanent miner = addCreatureReady(player1, new AethergeodeMiner());
        miner.tap();
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(findPermanent(player1, "Aethergeode Miner").getId()).isEqualTo(miner.getId());
        assertThat(miner.isTapped()).isTrue();

        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Aethergeode Miner");
        assertThat(returned.getId()).isNotEqualTo(miner.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotActivateWithOnlyOneEnergyCounter() {
        addCreatureReady(player1, new AethergeodeMiner());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsToItsOwnerRatherThanItsController() {
        AethergeodeMiner card = new AethergeodeMiner();
        card.setOwnerId(player2.getId());
        Permanent miner = addCreatureReady(player1, card);
        gd.stolenCreatures.put(miner.getId(), player2.getId());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aethergeode Miner");
        assertThat(findPermanent(player2, "Aethergeode Miner").getId()).isNotEqualTo(miner.getId());
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void earlierActivationCannotFlickerTheNewPermanent() {
        addCreatureReady(player1, new AethergeodeMiner());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();

        harness.passBothPriorities();
        UUID returnedId = findPermanent(player1, "Aethergeode Miner").getId();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Aethergeode Miner").getId()).isEqualTo(returnedId);
        assertThat(countPermanents(player1, "Aethergeode Miner")).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }
}
