package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaTheLastHope;
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

@CardUsed({
        OverlordOfTheBalemurk.class,
        AvatarOfMight.class,
        Forest.class,
        GrizzlyBears.class,
        LilianaTheLastHope.class
})
class OverlordOfTheBalemurkTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield mills four, then offers a graveyard return")
    void enteringMillsFourThenOffersGraveyardReturn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castNormally();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        int bearsIndex = choice.validIndices().stream()
                .filter(index -> gd.playerGraveyards.get(player1.getId()).get(index).getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        harness.handleGraveyardCardChosen(player1, bearsIndex);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The return includes planeswalkers but excludes Avatar creatures")
    void returnIncludesPlaneswalkersButExcludesAvatars() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new AvatarOfMight(), new LilianaTheLastHope()));
        castNormally();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).hasSize(1);
        int planeswalkerIndex = choice.validIndices().getFirst();
        assertThat(gd.playerGraveyards.get(player1.getId()).get(planeswalkerIndex).getName())
                .isEqualTo("Liliana, the Last Hope");
        harness.handleGraveyardCardChosen(player1, planeswalkerIndex);

        harness.assertInHand(player1, "Liliana, the Last Hope");
        harness.assertInGraveyard(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("Attacking mills four and offers a graveyard return")
    void attackingMillsFourThenOffersGraveyardReturn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new OverlordOfTheBalemurk());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting with impending enters with five time counters and is not a creature")
    void impendingCastEntersWithCountersAndIsNotCreature() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent overlord = castWithImpending();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(5);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    @DisplayName("Removing the last impending counter makes it a creature")
    void lastCounterMakesItCreature() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    private void castNormally() {
        harness.setHand(player1, List.of(new OverlordOfTheBalemurk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent castWithImpending() {
        harness.setHand(player1, List.of(new OverlordOfTheBalemurk()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }

        return findPermanent(player1, "Overlord of the Balemurk");
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
