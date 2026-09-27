package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousSquirrel.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class})
class RavenousSquirrelTest extends BaseCardTest {

    @Test
    void sacrificingACreaturePutsACounterOnRavenousSquirrel() {
        Permanent squirrel = addCreatureReady(player1, new RavenousSquirrel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        sacrifice(bears);
        resolveAllTriggers();

        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sacrificingAnArtifactPutsACounterOnRavenousSquirrel() {
        Permanent squirrel = addCreatureReady(player1, new RavenousSquirrel());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        sacrifice(fountain);
        resolveAllTriggers();

        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sacrificingANonArtifactNonCreatureDoesNotTrigger() {
        Permanent squirrel = addCreatureReady(player1, new RavenousSquirrel());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        sacrifice(forest);
        resolveAllTriggers();

        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activatedAbilitySacrificesAndGainsLifeAndDraws() {
        Permanent squirrel = addCreatureReady(player1, new RavenousSquirrel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void sacrifice(Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), card));
    }
}
