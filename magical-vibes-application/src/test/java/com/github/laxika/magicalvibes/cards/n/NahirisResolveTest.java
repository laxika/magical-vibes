package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NahirisResolve.class, GrizzlyBears.class, NullRod.class, Forest.class, RaiseTheAlarm.class,
        Threaten.class})
class NahirisResolveTest extends BaseCardTest {

    @Test
    void boostsAndGivesHasteToOwnCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new NahirisResolve());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void choosesNontokenArtifactsAndCreaturesAndReturnsThemAtNextUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NullRod());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        List<Permanent> tokens = findPermanents(player1, "Soldier");
        assertThat(tokens).hasSize(2);

        harness.addToBattlefield(player1, new NahirisResolve());
        advanceToEndStep();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId(), artifact.getId());
        assertThat(choice.validIds()).doesNotContain(land.getId(), opponentCreature.getId());
        assertThat(choice.validIds()).doesNotContain(tokens.stream().map(Permanent::getId).toArray(java.util.UUID[]::new));

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), artifact.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Null Rod");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Null Rod")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void mayChooseNoPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NahirisResolve());

        advanceToEndStep();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    void mayExileOnlySomeEligiblePermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NullRod());
        harness.addToBattlefield(player1, new NahirisResolve());

        advanceToEndStep();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Null Rod");
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NahirisResolve());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returnWaitsForControllersUpkeepAndSurvivesSourceLeaving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent resolve = harness.addToBattlefieldAndReturn(player1, new NahirisResolve());
        advanceToEndStep();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, resolve);

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    @Test
    void cardsWithDifferentOwnersReturnTogetherFromOneDelayedTrigger() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, stolenCreature.getId());
        harness.addToBattlefield(player1, new NahirisResolve());

        advanceToEndStep();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId(), stolenCreature.getId()));
        advanceToUpkeep(player2);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void doesNotReturnCardThatLeftExileAndWasExiledAgainBeforeUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NahirisResolve());
        advanceToEndStep();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.removeFromExile(creature.getCard().getId())).isTrue();
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        gd.playerGraveyards.get(player1.getId()).remove(creature.getCard());
        gd.addToExile(player1.getId(), creature.getCard());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
