package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlabornMusketeer.class, AirElemental.class})
class AlabornMusketeerTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Alaborn Musketeer block a creature with flying")
    void reachCanBlockFlyingCreature() {
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        flyer.setAttacking(true);
        Permanent musketeer = addCreatureReady(player2, new AlabornMusketeer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(musketeer.isBlocking()).isTrue();
    }
}
