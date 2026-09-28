package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SadisticShellGame.class, GrizzlyBears.class, Island.class})
class SadisticShellGameTest extends BaseCardTest {

    @Test
    void startsWithNextOpponentAndDestroysOneChosenCreaturePerPlayer() {
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castFromHand(player1, new SadisticShellGame(), "{4}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.validIds()).containsExactly(player1Creature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player1Creature.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player1.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2Creature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player2Creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(player1Land);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2Land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Creature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(player2Creature.getCard());
    }
}
