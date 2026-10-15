package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArastaOfTheEndlessWeb;
import com.github.laxika.magicalvibes.cards.e.ElspethSunsNemesis;
import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarelessCelebrant.class, FlameJavelin.class, LlanowarElves.class,
        ArastaOfTheEndlessWeb.class, ElspethSunsNemesis.class})
class CarelessCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("When Careless Celebrant dies, it deals 2 damage to an opponent's creature")
    void deathTriggerDamagesOpponentsCreature() {
        harness.addToBattlefield(player1, new CarelessCelebrant());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");

        killCelebrantWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("When Careless Celebrant dies, its trigger cannot target your creature")
    void deathTriggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new CarelessCelebrant());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID ownCreatureId = harness.getPermanentId(player1, "Llanowar Elves");
        UUID opponentCreatureId = harness.getPermanentId(player2, "Llanowar Elves");

        killCelebrantWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreatureId)
                .doesNotContain(ownCreatureId);
    }

    private void killCelebrantWithFlameJavelin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 5);

        UUID celebrantId = harness.getPermanentId(player1, "Careless Celebrant");
        harness.castAndResolveInstant(player2, 0, celebrantId);
    }

    @Test
    @DisplayName("Death trigger deals exactly two damage to a surviving creature")
    void deathTriggerDealsExactlyTwoDamage() {
        harness.addToBattlefield(player1, new CarelessCelebrant());
        harness.addToBattlefield(player2, new ArastaOfTheEndlessWeb());
        UUID targetId = harness.getPermanentId(player2, "Arasta of the Endless Web");

        killCelebrantWithFlameJavelin();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Arasta of the Endless Web");
        assertThat(gqs.findPermanentById(gd, targetId).getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death trigger damages an opposing planeswalker but cannot target your planeswalker")
    void deathTriggerTargetsOnlyOpposingPlaneswalker() {
        harness.addToBattlefield(player1, new CarelessCelebrant());
        harness.addToBattlefield(player1, new ElspethSunsNemesis());
        harness.addToBattlefield(player2, new ElspethSunsNemesis());
        UUID ownId = harness.getPermanentId(player1, "Elspeth, Sun's Nemesis");
        UUID targetId = harness.getPermanentId(player2, "Elspeth, Sun's Nemesis");
        gqs.findPermanentById(gd, ownId).setCounterCount(CounterType.LOYALTY, 5);
        gqs.findPermanentById(gd, targetId).setCounterCount(CounterType.LOYALTY, 5);
        int loyaltyBefore = gqs.findPermanentById(gd, targetId).getCounterCount(CounterType.LOYALTY);

        killCelebrantWithFlameJavelin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetId).doesNotContain(ownId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, targetId).getCounterCount(CounterType.LOYALTY))
                .isEqualTo(loyaltyBefore - 2);
        assertThat(gqs.findPermanentById(gd, ownId).getCounterCount(CounterType.LOYALTY))
                .isEqualTo(loyaltyBefore);
    }

    @Test
    @DisplayName("Death trigger with no legal opposing permanent has no target and deals no damage")
    void deathTriggerWithNoLegalTarget() {
        harness.addToBattlefield(player1, new CarelessCelebrant());
        harness.addToBattlefield(player1, new LlanowarElves());

        killCelebrantWithFlameJavelin();

        harness.assertInGraveyard(player1, "Careless Celebrant");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
