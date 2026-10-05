package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({MoorlandRescuer.class, GrizzlyBears.class, HillGiant.class, WrathOfGod.class,
        GloriousAnthem.class, GroundSeal.class, Nightmare.class, Swamp.class})
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3)
                .contains(bears, hillGiant);
    }

    @Test
    void choosesCreaturesOnlyWhenTheDeathAbilityResolves() {
        Card returned = new GrizzlyBears();
        harness.addToBattlefield(player1, new MoorlandRescuer());
        harness.setGraveyard(player1, List.of(returned));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).contains("Moorland Rescuer");
    }

    @Test
    void returnsCreaturesDespiteGroundSeal() {
        Card returned = new GrizzlyBears();
        Permanent rescuer = harness.addToBattlefieldAndReturn(player1, new MoorlandRescuer());
        harness.addToBattlefield(player2, new GroundSeal());
        harness.setGraveyard(player1, List.of(returned));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(returned.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rescuer.getCard());
    }

    @Test
    void usesPowerIncludingContinuousBoostsAtDeath() {
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        Permanent rescuer = harness.addToBattlefieldAndReturn(player1, new MoorlandRescuer());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setGraveyard(player1, List.of(bears, giant));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), giant.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rescuer.getCard());
    }

    @Test
    void returnsMultipleCreaturesWithTotalPowerExactlyEqualToItsPower() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Permanent rescuer = harness.addToBattlefieldAndReturn(player1, new MoorlandRescuer());
        harness.setGraveyard(player1, List.of(first, second));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rescuer.getCard());
    }

    @Test
    void exilesItselfWhenThereAreNoOtherCreatureCards() {
        Permanent rescuer = harness.addToBattlefieldAndReturn(player1, new MoorlandRescuer());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rescuer.getCard());
        harness.assertNotOnBattlefield(player1, "Moorland Rescuer");
    }

    @Test
    void countsCharacteristicDefinedPowerInTheGraveyard() {
        Card nightmare = new Nightmare();
        harness.addToBattlefield(player1, new MoorlandRescuer());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Swamp());
        }
        harness.setGraveyard(player1, List.of(nightmare));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nightmare.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Nightmare");
        harness.assertInGraveyard(player1, "Nightmare");
    }
}
