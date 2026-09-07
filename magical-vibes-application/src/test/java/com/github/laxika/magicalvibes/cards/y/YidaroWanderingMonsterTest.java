package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YidaroWanderingMonster.class, GrizzlyBears.class})
class YidaroWanderingMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("Below four cycles, shuffles Yidaro into the library before drawing")
    void belowThresholdShufflesBeforeDrawing() {
        YidaroWanderingMonster yidaro = new YidaroWanderingMonster();
        harness.setHand(player1, List.of(yidaro));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Yidaro, Wandering Monster");
        harness.assertNotInGraveyard(player1, "Yidaro, Wandering Monster");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("After four named cycles, puts Yidaro onto the battlefield instead")
    void thresholdReturnsToBattlefieldInsteadOfShuffling() {
        gd.recordCardCycled(player1.getId(), new YidaroWanderingMonster());
        gd.recordCardCycled(player1.getId(), new YidaroWanderingMonster());
        gd.recordCardCycled(player1.getId(), new YidaroWanderingMonster());

        YidaroWanderingMonster yidaro = new YidaroWanderingMonster();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(yidaro));
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yidaro, Wandering Monster");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
