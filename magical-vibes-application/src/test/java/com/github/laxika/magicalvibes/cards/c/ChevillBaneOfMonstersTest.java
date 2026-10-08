package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShatterTheSky;
import com.github.laxika.magicalvibes.cards.v.VivienMonstersAdvocate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChevillBaneOfMonsters.class, Forest.class, GrizzlyBears.class,
        AlmightyBrushwagg.class, ShatterTheSky.class, VivienMonstersAdvocate.class})
class ChevillBaneOfMonstersTest extends BaseCardTest {

    @Test
    void putsBountyCounterOnTargetOpponentCreatureAtUpkeep() {
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.BOUNTY)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerIfAnOpponentHasABountyCounter() {
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.BOUNTY, 1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersForBountiedOpponentPermanentAndDrawsACard() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setCounterCount(CounterType.BOUNTY, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, forest));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerForUnbountiedOrOwnPermanents() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownBears.setCounterCount(CounterType.BOUNTY, 1);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ownBears));
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opponentBears));
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canTargetOpponentPlaneswalkerButNotOwnCreatureOrOpponentLand() {
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        harness.addToBattlefield(player1, new AlmightyBrushwagg());
        harness.addToBattlefield(player2, new Forest());
        Permanent vivien = harness.addToBattlefieldAndReturn(player2, new VivienMonstersAdvocate());
        vivien.setCounterCount(CounterType.LOYALTY, 5);

        advanceToUpkeep(player1);

        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(vivien.getId());
        harness.handlePermanentChosen(player1, vivien.getId());
        harness.passBothPriorities();

        assertThat(vivien.getCounterCount(CounterType.BOUNTY)).isEqualTo(1);
    }

    @Test
    void opponentLandWithBountyPreventsUpkeepTrigger() {
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        harness.addToBattlefield(player2, new AlmightyBrushwagg());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setCounterCount(CounterType.BOUNTY, 1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownBountyDoesNotPreventUpkeepTrigger() {
        Permanent chevill = harness.addToBattlefieldAndReturn(player1, new ChevillBaneOfMonsters());
        chevill.setCounterCount(CounterType.BOUNTY, 1);
        Permanent brushwagg = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, brushwagg.getId());
        harness.passBothPriorities();

        assertThat(brushwagg.getCounterCount(CounterType.BOUNTY)).isEqualTo(1);
    }

    @Test
    void upkeepConditionIsCheckedAgainOnResolution() {
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent brushwagg = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, brushwagg.getId());
        forest.setCounterCount(CounterType.BOUNTY, 1);
        harness.passBothPriorities();

        assertThat(brushwagg.getCounterCount(CounterType.BOUNTY)).isZero();
    }

    @Test
    void gainsLifeAndDrawsWhenChevillAndBountiedCreatureDieSimultaneously() {
        harness.addToBattlefield(player1, new ChevillBaneOfMonsters());
        Permanent brushwagg = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        brushwagg.setCounterCount(CounterType.BOUNTY, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new ShatterTheSky(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chevill, Bane of Monsters");
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
        harness.assertLife(player1, 23);
        harness.assertInHand(player1, "Forest");
    }
}
