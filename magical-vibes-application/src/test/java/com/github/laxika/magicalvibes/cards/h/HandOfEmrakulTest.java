package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpawningPit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HandOfEmrakulTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by sacrificing four Spawn creatures")
    void castsBySacrificingFourSpawnCreatures() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        pit.setCounterCount(CounterType.CHARGE, 8);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
        }

        List<UUID> spawnIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(Permanent::getId)
                .toList();
        assertThat(spawnIds).hasSize(4);

        harness.setHand(player1, List.of(new HandOfEmrakul()));
        harness.castCreatureWithAlternateCost(player1, 0, spawnIds);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hand of Emrakul");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Attacking makes the defending player sacrifice a permanent")
    void attackTriggersAnnihilatorOne() {
        Permanent hand = addCreatureReady(player1, new HandOfEmrakul());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hand)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
