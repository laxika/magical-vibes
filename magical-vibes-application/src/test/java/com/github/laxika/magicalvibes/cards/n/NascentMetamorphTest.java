package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NascentMetamorph.class, GrizzlyBears.class, Island.class})
class NascentMetamorphTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking reveals an opponent's library and copies the found creature")
    void attacksAndCopiesFoundCreature() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        Card noncreature = new Island();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(noncreature, creature));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, metamorph)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, metamorph)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(noncreature.getId(), creature.getId());
    }

    @Test
    @DisplayName("Blocking also triggers the library reveal and copy")
    void blocksAndCopiesFoundCreature() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(creature));
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("A missing creature leaves the Metamorph unchanged and bottoms all revealed cards")
    void noCreatureFound() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Nascent Metamorph");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("The copy ends at cleanup")
    void copyEndsAtCleanup() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();
        assertThat(metamorph.getCard().getName()).isEqualTo("Grizzly Bears");

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(metamorph.getCard().getName()).isEqualTo("Nascent Metamorph");
    }

    @Test
    @DisplayName("Revealed cards go below the untouched remainder of the library")
    void revealedCardsGoBelowUnrevealedCards() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        Card revealedLand = new Island();
        Card foundCreature = new GrizzlyBears();
        Card untouchedLand = new Island();
        Card untouchedCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(revealedLand, foundCreature, untouchedLand, untouchedCreature));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2))
                .extracting(Card::getId)
                .containsExactly(untouchedLand.getId(), untouchedCreature.getId());
        assertThat(gd.playerDecks.get(player2.getId()).subList(2, 4))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(revealedLand.getId(), foundCreature.getId());
    }

    @Test
    @DisplayName("An empty library leaves the Metamorph unchanged")
    void emptyLibraryDoesNotCopyAnything() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        harness.setLibrary(player2, List.of());

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(metamorph.getCard().getName()).isEqualTo("Nascent Metamorph");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The library is still revealed and bottomed if the source has left")
    void sourceLeavingDoesNotStopLibraryEffect() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        Card creature = new GrizzlyBears();
        Card untouched = new Island();
        harness.setLibrary(player2, List.of(creature, untouched));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        gd.playerBattlefields.get(player1.getId()).remove(metamorph);
        harness.setGraveyard(player1, List.of(metamorph.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(untouched.getId(), creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Nascent Metamorph");
    }

    @Test
    @DisplayName("Copying preserves existing counters and attacking status")
    void copyingPreservesCountersAndCombatStatus() {
        Permanent metamorph = addCreatureReady(player1, new NascentMetamorph());
        metamorph.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        chooseOpponentTarget(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, metamorph)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, metamorph)).isEqualTo(4);
        assertThat(metamorph.isAttacking()).isTrue();
        assertThat(metamorph.isTapped()).isTrue();
    }

    private void chooseOpponentTarget(Player chooser) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(chooser, player2.getId());
    }
}
