package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ichorid.class, WalkingCorpse.class, GrizzlyBears.class})
class IchoridTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another black creature and returns Ichorid to the battlefield")
    void exilesAnotherBlackCreatureAndReturnsToBattlefield() {
        Ichorid ichorid = new Ichorid();
        WalkingCorpse fodder = new WalkingCorpse();
        GrizzlyBears nonblackCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ichorid, fodder, nonblackCreature));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(fodder.getId());
        harness.handleMultipleCardsChosen(player1, List.of(fodder.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(fodder.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ichorid.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(ichorid.getId()));
    }

    @Test
    @DisplayName("Declining the optional exile leaves Ichorid in the graveyard")
    void decliningExileLeavesIchoridInGraveyard() {
        Ichorid ichorid = new Ichorid();
        WalkingCorpse fodder = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(ichorid, fodder));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ichorid.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(ichorid.getId()));
    }

    @Test
    @DisplayName("Sacrifices itself at the end step")
    void sacrificesItselfAtEndStep() {
        Permanent ichorid = harness.addToBattlefieldAndReturn(player1, new Ichorid());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ichorid.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ichorid.getCard().getId()));
    }
}
