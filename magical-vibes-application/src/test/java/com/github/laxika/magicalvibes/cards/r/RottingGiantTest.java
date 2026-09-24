package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RottingGiant.class, Forest.class, WoodlandDruid.class})
class RottingGiantTest extends BaseCardTest {

    @Test
    void attackCanExileAnyCardFromGraveyard() {
        Permanent giant = addCreatureReady(player1, new RottingGiant());
        Card cardToKeep = new WoodlandDruid();
        Card cardToExile = new Forest();
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
        Card cardInGraveyard = new Forest();
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
        addCreatureReady(player2, new WoodlandDruid());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(giant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(giant.getCard());
    }

    @Test
    void blockingCanExileAnyCardFromGraveyard() {
        Permanent giant = addCreatureReady(player1, new RottingGiant());
        addCreatureReady(player2, new WoodlandDruid());
        Card cardToExile = new Forest();
        harness.setGraveyard(player1, List.of(cardToExile));

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).contains(cardToExile);
    }
}
