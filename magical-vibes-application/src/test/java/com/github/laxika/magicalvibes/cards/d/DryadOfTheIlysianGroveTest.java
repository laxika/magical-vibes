package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DryadOfTheIlysianGrove.class, Forest.class})
class DryadOfTheIlysianGroveTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may play one additional land each turn")
    void controllerGetsAdditionalLandPlay() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller's lands gain every basic land type and can produce any color")
    void controllerLandsGainBasicTypesAndAnyColorMana() {
        harness.addToBattlefield(player1, new DryadOfTheIlysianGrove());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(gqs.effectiveBasicLandTypes(gd, ownForest))
                .containsExactlyInAnyOrder(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, opponentForest))
                .containsExactly(CardSubtype.FOREST);

        harness.activateAbility(player1, 1, null, null);

        assertThat(ownForest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(opponentForest.isTapped()).isFalse();
    }
}
