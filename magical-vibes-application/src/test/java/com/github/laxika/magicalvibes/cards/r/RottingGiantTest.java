package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RottingGiantTest extends BaseCardTest {

    @Test
    void attackCanExileAnyCardFromGraveyard() {
        Permanent giant = addCreatureReady(player1, new RottingGiant());
        Card cardToKeep = new GrizzlyBears();
        Card cardToExile = new GiantGrowth();
        harness.setGraveyard(player1, List.of(cardToKeep, cardToExile));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cardToKeep);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).contains(cardToExile);
    }

    @Test
    void decliningToExileSacrificesAfterAttacking() {
        Permanent giant = addCreatureReady(player1, new RottingGiant());
        Card cardInGraveyard = new GiantGrowth();
        harness.setGraveyard(player1, List.of(cardInGraveyard));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(giant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cardInGraveyard, giant.getCard());
    }

    @Test
    void blockingWithEmptyGraveyardSacrifices() {
        Permanent giant = addCreatureReady(player1, new RottingGiant());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(giant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(giant.getCard());
    }
}
