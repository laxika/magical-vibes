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

import java.util.List;

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

    @Test
    void chosenCreatureEntersWithCountersWhenSeveralCardsAreExiled() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        Card chosen = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        gd.addToExile(player2.getId(), chosen, sothera.getId());
        gd.addToExile(player2.getId(), remaining, sothera.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sothera.getCard());
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == chosen)
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getCardsExiledByPermanent(sothera.getId())).containsExactly(remaining);
    }

    @Test
    void returnsExiledCreatureEvenIfSotheraLeavesBeforeResolution() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        Card exiledCreature = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiledCreature, sothera.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, sothera));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == exiledCreature)
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getCardsExiledByPermanent(sothera.getId())).isEmpty();
    }

    @Test
    void exilesTheOnlyOpposingCreatureAutomatically() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(sothera.getId())).containsExactly(opponentCreature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentCreature.getCard());
    }

    @Test
    void opponentsCreatureDyingDoesNotTriggerSothera() {
        harness.addToBattlefield(player1, new SotheraTheSupervoid());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dying));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
    }

    @Test
    void sacrificesEvenWhenNoCreatureCardIsExiledWithIt() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sothera.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(sothera);
    }
    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
