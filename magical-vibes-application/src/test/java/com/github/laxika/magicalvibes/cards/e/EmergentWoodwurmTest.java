package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmergentWoodwurm.class, Forest.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class EmergentWoodwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants another creature the power-scaled attack trigger")
    void backupGrantsAttackTriggerToAnotherCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, bears, shock));

        castWoodwurmTargeting(attacker);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        declareAttacker(attacker);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(forest, bears, shock);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, shock);
    }

    @Test
    @DisplayName("Backup targeting Emergent Woodwurm itself does not add a second attack trigger")
    void backupTargetingItselfDoesNotDuplicateAttackTrigger() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        Permanent woodwurm = castWoodwurmTargetingItself();

        assertThat(woodwurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        woodwurm.setSummoningSick(false);
        declareAttacker(woodwurm);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Backup's granted attack trigger expires at the end of the turn")
    void grantedAttackTriggerExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castWoodwurmTargeting(attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        declareAttacker(attacker);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void backingUpAnotherWoodwurmCreatesTwoSeparateAttackTriggers() {
        Permanent attacker = addCreatureReady(player1, new EmergentWoodwurm());
        castWoodwurmTargeting(attacker);

        declareAttacker(attacker);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void repeatedBackupGrantsSeparateAttackTriggersToTheSameCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castWoodwurmTargeting(attacker);
        castWoodwurmTargeting(attacker);

        declareAttacker(attacker);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void grantedAttackUsesRecipientPowerAndRejectsMoreExpensivePermanents() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castWoodwurmTargeting(attacker);
        Card expensive = new EmergentWoodwurm();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card secondForest = new Forest();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(expensive, forest, bears, shock, secondForest, untouched));

        declareAttacker(attacker);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(expensive, forest, bears, shock, secondForest);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                forest.getId(), bears.getId(), secondForest.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
    }

    @Test
    void nativeAttackLooksAtPowerCardsAndOnlyAllowsAffordablePermanents() {
        Permanent woodwurm = addCreatureReady(player1, new EmergentWoodwurm());
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card expensive = new EmergentWoodwurm();
        Card shock = new Shock();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(bears, forest, expensive, shock, untouched));

        declareAttacker(woodwurm);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(bears, forest, expensive, shock);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(entered.isTapped()).isFalse();
        assertThat(entered.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                untouched, forest, expensive, shock);
    }

    @Test
    void mayDeclineAndPutAllLookedAtCardsBelowUntouchedCards() {
        Permanent woodwurm = addCreatureReady(player1, new EmergentWoodwurm());
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card expensive = new EmergentWoodwurm();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(forest, bears, shock, expensive, untouched));

        declareAttacker(woodwurm);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                forest, bears, shock, expensive, untouched);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void attackUsesPowerAtResolutionForLookCountAndManaValueLimit() {
        Permanent woodwurm = addCreatureReady(player1, new EmergentWoodwurm());
        Card expensive = new EmergentWoodwurm();
        List<Card> topCards = List.of(expensive, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, java.util.stream.Stream.concat(
                topCards.stream(), java.util.stream.Stream.of(untouched)).toList());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttacker(woodwurm);
        harness.castAndResolveInstant(player1, 0, woodwurm.getId());
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactlyElementsOf(topCards);
        assertThat(choice.validCardIds()).contains(expensive.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
    }

    @Test
    void attackStillResolvesUsingLastKnownPowerAfterSourceDies() {
        Permanent woodwurm = addCreatureReady(player1, new EmergentWoodwurm());
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card expensive = new EmergentWoodwurm();
        Card shock = new Shock();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(bears, forest, expensive, shock, untouched));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttacker(woodwurm);
        harness.castAndResolveInstant(player1, 0, woodwurm.getId());
        harness.castAndResolveInstant(player1, 0, woodwurm.getId());
        harness.assertInGraveyard(player1, "Emergent Woodwurm");
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(bears, forest, expensive, shock);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
    }

    @Test
    void backupCanTargetAnOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        castWoodwurmTargeting(opponentCreature);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void noEligibleCardsAreReturnedToBottomWithoutAChoice() {
        Permanent woodwurm = addCreatureReady(player1, new EmergentWoodwurm());
        Card shock = new Shock();
        Card expensive = new EmergentWoodwurm();
        harness.setLibrary(player1, List.of(shock, expensive));

        declareAttacker(woodwurm);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(shock, expensive);
    }

    private Permanent castWoodwurmTargeting(Permanent target) {
        harness.setHand(player1, List.of(new EmergentWoodwurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Emergent Woodwurm");
    }

    private Permanent castWoodwurmTargetingItself() {
        harness.setHand(player1, List.of(new EmergentWoodwurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent woodwurm = findPermanent(player1, "Emergent Woodwurm");
        harness.handlePermanentChosen(player1, woodwurm.getId());
        harness.passBothPriorities();
        return woodwurm;
    }

    private void declareAttacker(Permanent attacker) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
    }
}
