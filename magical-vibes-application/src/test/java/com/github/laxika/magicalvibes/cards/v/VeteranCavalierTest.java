package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VeteranCavalier.class)
class VeteranCavalierTest extends BaseCardTest {

    @Test
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent veteranCavalier = addCreatureReady(player1, new VeteranCavalier());

        declareAttackers(List.of(0));

        assertThat(veteranCavalier.isTapped()).isFalse();
    }
}
