package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArtilleryEnthusiast.class, GrizzlyBears.class, HillGiant.class,
        LoxodonWarhammer.class, Pacifism.class})
class ArtilleryEnthusiastTest extends BaseCardTest {

    @Test
    void modifiedCreaturesYouControlHaveMenace() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        Permanent modified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unmodified = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentModified = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentModified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modified, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodified, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentModified, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isFalse();
    }

    @Test
    void mayDiscardToSeekCardWithTheDiscardedCardsManaValue() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears matching = new GrizzlyBears();
        HillGiant nonmatching = new HillGiant();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(nonmatching, matching));

        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    void decliningDiscardLeavesHandAndLibraryUnchanged() {
        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        GrizzlyBears card = new GrizzlyBears();
        HillGiant libraryCard = new HillGiant();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void seekingCompletesDuringTheSameResolutionAsDiscarding() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears matching = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(matching));
        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matching);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardingWithoutAMatchingCardDoesNotSeekAnotherManaValue() {
        GrizzlyBears discarded = new GrizzlyBears();
        HillGiant nonmatching = new HillGiant();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(nonmatching));
        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    void acceptingWithAnEmptyHandDoesNotSeek() {
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardingWithAnEmptyLibraryStillDiscardsAndFinishesResolving() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentControlledEnthusiastDiscardsAndSeeksOnlyForItsController() {
        GrizzlyBears untouchedHand = new GrizzlyBears();
        GrizzlyBears untouchedLibrary = new GrizzlyBears();
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears matching = new GrizzlyBears();
        harness.setHand(player1, List.of(untouchedHand));
        harness.setLibrary(player1, List.of(untouchedLibrary));
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(matching));
        harness.enterBattlefieldAndReturn(player2, new ArtilleryEnthusiast());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(matching);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(untouchedHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sourceGainsAndLosesMenaceAsItsCountersChange() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isTrue();

        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isFalse();
    }

    @Test
    void anyCounterModifiesACreatureAndMenaceEndsWhenTheSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(source);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    void seekingMovesExactlyOneMatchingCardAndPreservesOtherLibraryCards() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears firstMatch = new GrizzlyBears();
        GrizzlyBears secondMatch = new GrizzlyBears();
        HillGiant nonmatching = new HillGiant();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(firstMatch, nonmatching, secondMatch));
        harness.enterBattlefieldAndReturn(player1, new ArtilleryEnthusiast());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        var sought = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(sought).isIn(firstMatch, secondMatch);
        var remaining = sought == firstMatch ? secondMatch : firstMatch;
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(
                sought == firstMatch ? List.of(nonmatching, remaining) : List.of(remaining, nonmatching));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void equipmentModifiesACreatureRegardlessOfItsController() {
        harness.addToBattlefield(player1, new ArtilleryEnthusiast());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        equipment.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    void onlyAnAuraControlledByTheCreatureControllerModifiesIt() {
        harness.addToBattlefield(player1, new ArtilleryEnthusiast());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        opposingAura.setAttachedTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();

        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        ownAura.setAttachedTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(ownAura);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }
}
