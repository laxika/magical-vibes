package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SotheraTheSupervoid.class, GrizzlyBears.class})
class SotheraTheSupervoidTest extends BaseCardTest {

    @Test
    void eachOpponentChoosesAndExilesACreatureWithSothera() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, chosen.getId());

        assertThat(gd.getCardsExiledByPermanent(sothera.getId())).containsExactly(chosen.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    void sacrificesAndReturnsAnExiledCreatureIfAnyPlayerControlsNoCreatures() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        Card exiledCreature = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiledCreature, sothera.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sothera.getCard());
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == exiledCreature)
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getCardsExiledByPermanent(sothera.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWhenEveryPlayerControlsACreature() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sothera);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNothingIfEveryPlayerControlsACreatureWhenTheTriggerResolves() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        Card exiledCreature = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiledCreature, sothera.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sothera);
        assertThat(gd.getCardsExiledByPermanent(sothera.getId())).containsExactly(exiledCreature);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
