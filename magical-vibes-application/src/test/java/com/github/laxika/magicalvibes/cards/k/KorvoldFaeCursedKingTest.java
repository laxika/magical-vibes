package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KorvoldFaeCursedKing.class, Forest.class, GrizzlyBears.class, DiabolicEdict.class, BarterInBlood.class})
class KorvoldFaeCursedKingTest extends BaseCardTest {

    @Test
    void entersAndSacrificesAnotherPermanentThenGrowsAndDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new KorvoldFaeCursedKing(), "{2}{B}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, forest.getId());
        resolveAllTriggers();

        Permanent korvold = findPermanent(player1, "Korvold, Fae-Cursed King");
        assertThat(korvold.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void attacksAndSacrificesAnotherPermanent() {
        Permanent korvold = addCreatureReady(player1, new KorvoldFaeCursedKing());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, forest.getId());
        resolveAllTriggers();

        assertThat(korvold.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void sacrificingKorvoldDrawsFromItsSacrificeTrigger() {
        addCreatureReady(player1, new KorvoldFaeCursedKing());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .contains("Korvold, Fae-Cursed King");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        harness.assertInGraveyard(player1, "Korvold, Fae-Cursed King");
    }

    @Test
    void enteringAloneDoesNotSacrificeKorvoldOrDraw() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new KorvoldFaeCursedKing(), "{2}{B}{R}{G}");

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Korvold, Fae-Cursed King");
        assertThat(findPermanent(player1, "Korvold, Fae-Cursed King")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackingAloneDoesNotSacrificeKorvoldOrDraw() {
        Permanent korvold = addCreatureReady(player1, new KorvoldFaeCursedKing());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Korvold, Fae-Cursed King");
        assertThat(korvold.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsSacrificeDoesNotGrowKorvoldOrDraw() {
        Permanent korvold = addCreatureReady(player1, new KorvoldFaeCursedKing());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(korvold.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void simultaneousSacrificeOfKorvoldAndAnotherCreatureDrawsExactlyTwoCards() {
        harness.addToBattlefield(player1, new KorvoldFaeCursedKing());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Korvold, Fae-Cursed King");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void simultaneousSacrificeOfTwoOtherCreaturesGrowsKorvoldTwiceAndDrawsTwice() {
        Permanent korvold = harness.addToBattlefieldAndReturn(player1, new KorvoldFaeCursedKing());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Korvold, Fae-Cursed King");
        assertThat(korvold.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
