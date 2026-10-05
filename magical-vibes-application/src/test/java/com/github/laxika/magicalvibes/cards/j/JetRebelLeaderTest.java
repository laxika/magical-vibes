package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.e.EmptyCityRuse;
import com.github.laxika.magicalvibes.cards.k.KomodoRhino;
import com.github.laxika.magicalvibes.cards.k.KyoshiWarriorGuard;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JetRebelLeader.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        EmptyCityRuse.class, KomodoRhino.class, KyoshiWarriorGuard.class})
class JetRebelLeaderTest extends BaseCardTest {

    @Test
    void putsEligibleCreatureTappedAndAttacking() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS,
                TurnStep.DECLARE_BLOCKERS));

        addCreatureReady(player1, new JetRebelLeader());
        Card eligibleCreature = new GrizzlyBears();
        Card tooExpensiveCreature = new HillGiant();
        Card nonCreature = new Shock();
        Card otherCard = new Shock();
        Card fifthCard = new Shock();
        harness.setLibrary(player1, List.of(
                tooExpensiveCreature,
                nonCreature,
                eligibleCreature,
                otherCard,
                fifthCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice libraryChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(libraryChoice).isNotNull();
        assertThat(libraryChoice.validCardIds()).containsExactly(eligibleCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligibleCreature.getId()));

        Permanent enteredCreature = findPermanent(player1, eligibleCreature.getName());
        assertThat(enteredCreature.isTapped()).isTrue();
        assertThat(enteredCreature.isAttacking()).isTrue();
        assertThat(enteredCreature.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    void decliningCreatureBottomsOnlyTheFiveLookedAtCards() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new JetRebelLeader());
        Card creature = new KyoshiWarriorGuard();
        List<Card> lookedAt = List.of(creature, new EmptyCityRuse(), new KomodoRhino(),
                new EmptyCityRuse(), new EmptyCityRuse());
        Card untouched = new KyoshiWarriorGuard();
        harness.setLibrary(player1, List.of(lookedAt.get(0), lookedAt.get(1), lookedAt.get(2),
                lookedAt.get(3), lookedAt.get(4), untouched));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6);
        assertThat(library.getFirst()).isSameAs(untouched);
        assertThat(library.subList(1, 6)).containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotChooseMoreThanOneCreatureFromAShortLibrary() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new JetRebelLeader());
        Card first = new KyoshiWarriorGuard();
        Card second = new KyoshiWarriorGuard();
        Card expensive = new KomodoRhino();
        harness.setLibrary(player1, List.of(first, second, expensive));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        Permanent entered = findPermanent(player1, first.getName());
        assertThat(entered.getCard()).isSameAs(first);
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(second, expensive);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void noEligibleCreatureInShortLibraryRequiresNoChoice() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new JetRebelLeader());
        Card expensive = new KomodoRhino();
        Card nonCreature = new EmptyCityRuse();
        harness.setLibrary(player1, List.of(expensive, nonCreature));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(expensive, nonCreature);
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS));
        addCreatureReady(player1, new JetRebelLeader());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
