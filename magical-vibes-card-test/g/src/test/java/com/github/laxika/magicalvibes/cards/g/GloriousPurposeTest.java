package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BigScore;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HypnoticGrifter;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloriousPurpose.class, HypnoticGrifter.class, Forest.class, Mountain.class, BigScore.class})
class GloriousPurposeTest extends BaseCardTest {

    @Test
    void connivingCreatureGetsCounterAndPurposeGetsPlanCounter() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        Permanent grifter = addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new HypnoticGrifter()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(purpose.getCounterCount(CounterType.PLAN)).isEqualTo(1);
    }

    @Test
    void sixthPlanCounterSacrificesPurposeAndReturnsUncastCardsToHand() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        purpose.setCounterCount(CounterType.PLAN, 5);
        addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new Mountain()));
        Card drawnForConnive = new Mountain();
        Card freeCastCreature = new HypnoticGrifter();
        Card firstRest = new Forest();
        Card secondRest = new Forest();
        Card thirdRest = new Forest();
        harness.setLibrary(player1, List.of(drawnForConnive, freeCastCreature, firstRest, secondRest, thirdRest));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glorious Purpose");
        harness.assertInGraveyard(player1, "Glorious Purpose");
        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactly(freeCastCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(freeCastCreature.getId()));

        assertThat(gd.findExiledCard(freeCastCreature.getId())).isNull();
        assertThat(gd.findExiledCard(firstRest.getId())).isNull();
        assertThat(gd.findExiledCard(secondRest.getId())).isNull();
        assertThat(gd.findExiledCard(thirdRest.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstRest, secondRest, thirdRest);
    }

    @Test
    void discardingLandStillAddsThePurposeCounter() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        Permanent grifter = addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(purpose.getCounterCount(CounterType.PLAN)).isEqualTo(1);
    }

    @Test
    void opponentsConniveDoesNotTriggerPurpose() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        Permanent grifter = addCreatureReady(player2, new HypnoticGrifter());
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(grifter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(purpose.getCounterCount(CounterType.PLAN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingPlanCounterAfterSixthCounterTriggerDoesNotPreventSacrifice() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        purpose.setCounterCount(CounterType.PLAN, 5);
        addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new Mountain()));
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(new Mountain(), first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(purpose.getCounterCount(CounterType.PLAN)).isEqualTo(6);
        purpose.setCounterCount(CounterType.PLAN, 5);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glorious Purpose");
        harness.assertInGraveyard(player1, "Glorious Purpose");
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningAllFreeSpellsReturnsEveryExiledCardToHand() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        purpose.setCounterCount(CounterType.PLAN, 5);
        addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new Mountain()));
        Card spell = new HypnoticGrifter();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(new Mountain(), spell, land));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.ImprovisationCapstoneCastChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).contains(spell, land);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void freeCastingAllowsSpellsWithPayableAdditionalCosts() {
        Permanent purpose = harness.addToBattlefieldAndReturn(player1, new GloriousPurpose());
        purpose.setCounterCount(CounterType.PLAN, 5);
        addCreatureReady(player1, new HypnoticGrifter());
        harness.setHand(player1, List.of(new Mountain()));
        Card spell = new BigScore();
        harness.setLibrary(player1, List.of(new Mountain(), spell, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).contains(spell.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.interaction.isAwaitingInput()
                || gd.stack.stream().anyMatch(entry -> entry.getCard().getId().equals(spell.getId())))
                .isTrue();
    }
}
