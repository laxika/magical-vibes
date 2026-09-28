package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HearthKami.class, HondenOfInfiniteRage.class, HondenOfLifesWeb.class})
class HondenOfInfiniteRageTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger deals damage to a player equal to the number of Shrines controlled")
    void dealsDamageToPlayerForEachShrine() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        resolveDamageTo(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Upkeep trigger can deal its damage to a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        Permanent hearthKami = harness.addToBattlefieldAndReturn(player2, new HearthKami());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        resolveDamageTo(hearthKami.getId());

        assertThat(gqs.findPermanentById(gd, hearthKami.getId())).isNull();
    }

    @Test
    @DisplayName("Shrines an opponent controls are not counted")
    void ignoresOpponentShrines() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        harness.addToBattlefield(player2, new HondenOfLifesWeb());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveDamageTo(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HondenOfInfiniteRage());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void resolveDamageTo(UUID targetId) {
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
