package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YidaroWanderingMonster.class, AlmightyBrushwagg.class})
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

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(yidaro);
        harness.assertNotInGraveyard(player1, "Yidaro, Wandering Monster");

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
        AlmightyBrushwagg brushwagg = new AlmightyBrushwagg();
        harness.setHand(player1, List.of(yidaro));
        harness.setLibrary(player1, List.of(brushwagg));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Yidaro, Wandering Monster");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(brushwagg);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Almighty Brushwagg");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's named cycles do not count toward your threshold")
    void opponentsCyclesDoNotCount() {
        for (int i = 0; i < 4; i++) {
            gd.recordCardCycled(player2.getId(), new YidaroWanderingMonster());
        }
        YidaroWanderingMonster yidaro = new YidaroWanderingMonster();
        harness.setHand(player1, List.of(yidaro));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yidaro, Wandering Monster");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(yidaro);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Yidaro, Wandering Monster");
    }

    @Test
    @DisplayName("Above the threshold, only the cycled Yidaro returns from the graveyard")
    void aboveThresholdReturnsOnlyCycledCard() {
        for (int i = 0; i < 4; i++) {
            gd.recordCardCycled(player1.getId(), new YidaroWanderingMonster());
        }
        YidaroWanderingMonster otherYidaro = new YidaroWanderingMonster();
        YidaroWanderingMonster cycledYidaro = new YidaroWanderingMonster();
        harness.setGraveyard(player1, List.of(otherYidaro));
        harness.setHand(player1, List.of(cycledYidaro));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(cycledYidaro.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherYidaro);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Almighty Brushwagg");
    }
}
