package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThranDynamoTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Thran Dynamo produces three colorless mana")
    void tappingProducesThreeColorlessMana() {
        Permanent dynamo = new Permanent(new ThranDynamo());
        dynamo.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dynamo);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(dynamo.isTapped()).isTrue();
    }
}
