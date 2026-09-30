package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoorlandRescuer.class, GrizzlyBears.class, HillGiant.class, WrathOfGod.class})
class MoorlandRescuerTest extends BaseCardTest {

    @Test
    void returnsOtherCreaturesAndExilesItself() {
        Card returned = new GrizzlyBears();
        Permanent rescuer = harness.addToBattlefieldAndReturn(player1, new MoorlandRescuer());
        harness.setGraveyard(player1, List.of(returned));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returned.getId());
        assertThat(choice.maxTotalPower()).isEqualTo(4);

        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rescuer.getCard());
    }

    @Test
    void enforcesTotalPowerLimit() {
        Card bears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Permanent rescuer = harness.addToBattlefieldAndReturn(player1, new MoorlandRescuer());
        harness.setGraveyard(player1, List.of(bears, hillGiant));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(bears.getId(), hillGiant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 4");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rescuer.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, hillGiant);
    }
}
