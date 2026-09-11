package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonsDisciple.class, GrizzlyBears.class, ChandraNalaar.class, Forest.class})
@DisplayName("Demon's Disciple")
class DemonsDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses a creature and sacrifices all choices together")
    void eachPlayerChoosesCreatureToSacrifice() {
        Permanent player1Sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDemonsDisciple();

        GameData gameData = harness.getGameData();
        PendingInteraction.MultiPermanentChoice player1Choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Sacrifice.getId()));
        PendingInteraction.MultiPermanentChoice player2Choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Sacrifice.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Demon's Disciple");
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A planeswalker is eligible while a noncreature nonplaneswalker is not")
    void sacrificesPlaneswalkerAndLeavesForest() {
        harness.addToBattlefield(player2, new ChandraNalaar());
        harness.addToBattlefield(player2, new Forest());
        castDemonsDisciple();

        harness.assertInGraveyard(player1, "Demon's Disciple");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    private void castDemonsDisciple() {
        harness.setHand(player1, List.of(new DemonsDisciple()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
