package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrimclimbPathway;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightclimbPathway.class, GrimclimbPathway.class})
class BrightclimbPathwayTest extends BaseCardTest {

    @Test
    void playingFrontFaceProducesWhiteMana() {
        harness.setHand(player1, List.of(new BrightclimbPathway()));

        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(mana.get(ManaColor.BLACK)).isZero();
    }

    @Test
    void playingBackFaceProducesBlackMana() {
        harness.setHand(player1, List.of(new BrightclimbPathway()));

        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(mana.get(ManaColor.WHITE)).isZero();
    }
}
