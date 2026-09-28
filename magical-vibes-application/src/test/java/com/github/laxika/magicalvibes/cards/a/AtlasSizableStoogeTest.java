package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtlasSizableStooge.class, GrizzlyBears.class, SerraAngel.class})
class AtlasSizableStoogeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gains life for each creature you control with power 4 or greater")
    void attackingGainsLifeForQualifyingCreatures() {
        addCreatureReady(player1, new AtlasSizableStooge());
        addCreatureReady(player1, new SerraAngel());
        addCreatureReady(player1, new GrizzlyBears());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Blocking gains life for each creature you control with power 4 or greater")
    void blockingGainsLifeForQualifyingCreatures() {
        addCreatureReady(player1, new AtlasSizableStooge());
        addCreatureReady(player1, new SerraAngel());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }
}
