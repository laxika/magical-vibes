package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SotheraTheSupervoid.class, GrizzlyBears.class})
class SotheraTheSupervoidTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent chooses a creature they control and it is exiled with Sothera")
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
    @DisplayName("Every opponent chooses before the chosen creatures are exiled together (CR 101.4)")
    void opponentsChooseBeforeAnyCreatureIsExiled() {
        Player player3 = addThirdPlayer();
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstChosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent secondChosen = harness.addToBattlefieldAndReturn(player3, new GrizzlyBears());
        harness.addToBattlefield(player3, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, firstChosen.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstChosen);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player3.getId());

        harness.handlePermanentChosen(player3, secondChosen.getId());

        assertThat(gd.getCardsExiledByPermanent(sothera.getId()))
                .containsExactlyInAnyOrder(firstChosen.getCard(), secondChosen.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstChosen);
        assertThat(gd.playerBattlefields.get(player3.getId())).doesNotContain(secondChosen);
    }

    private Player addThirdPlayer() {
        Player player3 = new Player(UUID.randomUUID(), "Third opponent");
        gd.playerIds.add(player3.getId());
        gd.orderedPlayerIds.add(player3.getId());
        gd.playerNames.add(player3.getUsername());
        gd.playerIdToName.put(player3.getId(), player3.getUsername());
        gd.playerDecks.put(player3.getId(), new ArrayList<>());
        gd.playerHands.put(player3.getId(), new ArrayList<>());
        gd.playerGraveyards.put(player3.getId(), new ArrayList<>());
        gd.playerBattlefields.put(player3.getId(), new ArrayList<>());
        gd.playerManaPools.put(player3.getId(), new ManaPool());
        gd.playerLifeTotals.put(player3.getId(), 20);
        return player3;
    }

    @Test
    @DisplayName("If a player controls no creatures, Sothera is sacrificed and an exiled creature returns")
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
    @DisplayName("Does not trigger while every player controls a creature")
    void doesNotTriggerWhenEveryPlayerControlsACreature() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sothera);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does nothing if every player controls a creature when the trigger resolves")
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
    @DisplayName("The chosen creature card enters with counters when several cards are exiled")
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
    @DisplayName("Returns an exiled creature even if Sothera leaves before the trigger resolves")
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
    @DisplayName("An opponent's only creature is exiled without a prompt")
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
    @DisplayName("An opponent's creature dying does not trigger Sothera")
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
    @DisplayName("Sothera is sacrificed even when no creature card is exiled with it")
    void sacrificesEvenWhenNoCreatureCardIsExiledWithIt() {
        Permanent sothera = harness.addToBattlefieldAndReturn(player1, new SotheraTheSupervoid());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sothera.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
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
