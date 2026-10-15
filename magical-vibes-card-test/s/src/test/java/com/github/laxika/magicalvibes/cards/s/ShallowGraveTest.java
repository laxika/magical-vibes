package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShallowGrave.class, IronTuskElephant.class, FeralShadow.class, Incinerate.class})
class ShallowGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the topmost creature card of your graveyard with haste")
    void returnsTopmostCreatureWithHaste() {
        Card bottom = new IronTuskElephant();
        Card top = new FeralShadow();
        harness.setGraveyard(player1, List.of(bottom, top));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Feral Shadow");
        harness.assertNotOnBattlefield(player1, "Iron Tusk Elephant");
        harness.assertInGraveyard(player1, "Iron Tusk Elephant");

        Permanent returned = findPermanent(player1, "Feral Shadow");
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Skips non-creature cards above the topmost creature card")
    void skipsNonCreatureCardsAboveIt() {
        Card creature = new IronTuskElephant();
        harness.setGraveyard(player1, List.of(creature, new Incinerate()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iron Tusk Elephant");
        harness.assertInGraveyard(player1, "Incinerate");
    }

    @Test
    @DisplayName("The returned creature is exiled at the beginning of the next end step")
    void returnedCreatureExiledAtEndStep() {
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Iron Tusk Elephant");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iron Tusk Elephant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Iron Tusk Elephant"));
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().equals("Iron Tusk Elephant is exiled."));
    }

    @Test
    @DisplayName("The delayed exile waits for resolution after the next end step begins")
    void delayedExileWaitsForResolution() {
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");

        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Iron Tusk Elephant");
    }

    @Test
    @DisplayName("Unlike Unearth, a creature that dies before the end step is not exiled")
    void creatureDyingBeforeEndStepGoesToGraveyard() {
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");

        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Iron Tusk Elephant");
        assertThat(returned.isExileIfLeavesBattlefield()).isFalse();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Iron Tusk Elephant");
        harness.assertInGraveyard(player1, "Iron Tusk Elephant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Iron Tusk Elephant"));
    }

    @Test
    @DisplayName("Does nothing when the graveyard holds no creature card")
    void doesNothingWithoutCreatureCard() {
        harness.setGraveyard(player1, List.of(new Incinerate()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shallow Grave");
    }

    @Test
    @DisplayName("Casting during the end step waits until the following end step to exile")
    void castDuringEndStepWaitsUntilFollowingEndStep() {
        harness.passUntil(TurnStep.END_STEP);
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player1, "Iron Tusk Elephant");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Iron Tusk Elephant");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iron Tusk Elephant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Iron Tusk Elephant"));
    }

    @Test
    @DisplayName("Haste expires at cleanup even when the delayed exile is still pending")
    void hasteExpiresBeforeFollowingTurn() {
        harness.passUntil(TurnStep.END_STEP);
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Iron Tusk Elephant").hasKeyword(Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Iron Tusk Elephant");
        assertThat(findPermanent(player1, "Iron Tusk Elephant").hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not return a creature from the opponent's graveyard")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new Incinerate()));
        harness.setGraveyard(player2, List.of(new IronTuskElephant()));
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iron Tusk Elephant");
        harness.assertNotOnBattlefield(player2, "Iron Tusk Elephant");
        harness.assertInGraveyard(player2, "Iron Tusk Elephant");
    }

    @Test
    @DisplayName("Selects the top creature at resolution after another creature dies in response")
    void selectsTopCreatureAtResolution() {
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));
        Permanent shadow = harness.addToBattlefieldAndReturn(player1, new FeralShadow());
        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, shadow.getId());
        harness.assertInGraveyard(player1, "Feral Shadow");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Feral Shadow");
        harness.assertInGraveyard(player1, "Iron Tusk Elephant");
        harness.assertNotOnBattlefield(player1, "Iron Tusk Elephant");
        assertThat(findPermanent(player1, "Feral Shadow").hasKeyword(Keyword.HASTE)).isTrue();
    }
}
