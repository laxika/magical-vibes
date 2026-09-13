package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StandingTroops.class})
class StandingTroopsTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent standingTroops = addCreatureReady(player1, new StandingTroops());

        declareAttackers(List.of(0));

        assertThat(standingTroops.isTapped()).isFalse();
    }
}
