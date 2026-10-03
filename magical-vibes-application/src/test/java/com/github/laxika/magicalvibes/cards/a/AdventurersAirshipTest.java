package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VincentsLimitBreak;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdventurersAirship.class, Forest.class, GrizzlyBears.class, VincentsLimitBreak.class})
class AdventurersAirshipTest extends BaseCardTest {

    @Test
    void crewAnimatesTheVehicleAndTapsTheCrew() {
        Permanent airship = addCreatureReady(player1, new AdventurersAirship());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, airship)).isTrue();
        assertThat(bear.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, airship)).isFalse();
    }

    @Test
    void attackingDrawsThenDiscards() {
        addCreatureReady(player1, new AdventurersAirship());
        addCreatureReady(player1, new GrizzlyBears());
        Card cardToDiscard = new GrizzlyBears();
        Card cardToDraw = new Forest();
        harness.setHand(player1, List.of(cardToDiscard));
        harness.setLibrary(player1, List.of(cardToDraw));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cardToDiscard);
        assertThat(gd.playerHands.get(player1.getId())).contains(cardToDraw);
    }

    @Test
    void mayDiscardTheCardJustDrawn() {
        addCreatureReady(player1, new AdventurersAirship());
        addCreatureReady(player1, new GrizzlyBears());
        Card keptCard = new GrizzlyBears();
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of(keptCard));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void attackingWithAnEmptyHandStillDrawsThenDiscards() {
        addCreatureReady(player1, new AdventurersAirship());
        addCreatureReady(player1, new GrizzlyBears());
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void summoningSickCreatureCanCrewWithoutTriggeringLoot() {
        Permanent airship = addCreatureReady(player1, new AdventurersAirship());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(true);
        Card handCard = new Forest();
        Card libraryCard = new Forest();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, airship)).isTrue();
        assertThat(airship.isTapped()).isFalse();
        assertThat(bear.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void crewingAgainPreservesAnExistingBasePowerAndToughnessEffect() {
        Permanent airship = addCreatureReady(player1, new AdventurersAirship());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new VincentsLimitBreak()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 1, airship.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, airship)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, airship)).isEqualTo(2);

        bear.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, airship)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, airship)).isEqualTo(2);
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        Permanent airship = addCreatureReady(player1, new AdventurersAirship());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, airship)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
