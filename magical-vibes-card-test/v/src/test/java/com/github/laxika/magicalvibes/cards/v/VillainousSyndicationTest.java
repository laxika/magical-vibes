package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomsServoGuards;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MODOK;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillainousSyndication.class, MODOK.class, Forest.class, GrizzlyBears.class, DoomsServoGuards.class, VampireHexmage.class})
class VillainousSyndicationTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Villain mills a card and puts a plan counter on Villainous Syndication")
    void tapsVillainMillsAndAddsPlanCounter() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        Permanent villain = addCreatureReady(player1, new MODOK());
        Forest milledCard = new Forest();
        harness.setLibrary(player1, List.of(milledCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(villain.isTapped()).isTrue();
        assertThat(syndication.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
    }

    @Test
    @DisplayName("The fourth plan counter sacrifices Villainous Syndication and returns a target creature")
    void fourthPlanCounterSacrificesAndReturnsCreature() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        syndication.setCounterCount(CounterType.PLAN, 3);
        Permanent villain = addCreatureReady(player1, new MODOK());
        Forest milledCard = new Forest();
        GrizzlyBears returnedCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milledCard));
        harness.setGraveyard(player1, List.of(returnedCreature));

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(villain.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(syndication);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(syndication.getCard());
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(returnedCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(returnedCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ability is restricted to sorcery speed")
    void abilityIsSorcerySpeedOnly() {
        harness.addToBattlefield(player1, new VillainousSyndication());
        addCreatureReady(player1, new MODOK());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Removing plan counters in response does not stop the sacrifice and return")
    void removingCountersDoesNotUndoFourthCounterTrigger() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        syndication.setCounterCount(CounterType.PLAN, 3);
        addCreatureReady(player1, new MODOK());
        harness.addToBattlefield(player1, new VampireHexmage());
        GrizzlyBears returnedCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(syndication.getCounterCount(CounterType.PLAN)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 2, null, syndication.getId());
        harness.passBothPriorities();
        assertThat(syndication.getCounterCount(CounterType.PLAN)).isZero();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Villainous Syndication");
        harness.assertInGraveyard(player1, "Villainous Syndication");
        harness.handleMultipleCardsChosen(player1, List.of(returnedCreature.getId()));
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A summoning-sick Villain pays the tap cost before the ability resolves")
    void summoningSickVillainCanPayTapCost() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DoomsServoGuards());
        VillainousSyndication milledCard = new VillainousSyndication();
        harness.setLibrary(player1, List.of(milledCard));
        assertThat(villain.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, null, null);

        assertThat(villain.isTapped()).isTrue();
        assertThat(syndication.getCounterCount(CounterType.PLAN)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(milledCard);
        resolveAllTriggers();
        assertThat(syndication.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
    }

    @Test
    @DisplayName("An empty library does not prevent putting a plan counter")
    void emptyLibraryStillGetsPlanCounter() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        harness.addToBattlefield(player1, new DoomsServoGuards());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(syndication.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped Villain cannot pay the activation cost")
    void tappedVillainCannotPayCost() {
        harness.addToBattlefield(player1, new VillainousSyndication());
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DoomsServoGuards());
        villain.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Villain cannot pay the activation cost")
    void opponentsVillainCannotPayCost() {
        harness.addToBattlefield(player1, new VillainousSyndication());
        harness.addToBattlefield(player2, new DoomsServoGuards());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A non-Villain cannot pay the activation cost")
    void nonVillainCannotPayCost() {
        harness.addToBattlefield(player1, new VillainousSyndication());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The freshly milled creature can be returned, with targets chosen after sacrifice")
    void fourthActivationCanReturnFreshlyMilledCreature() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        syndication.setCounterCount(CounterType.PLAN, 3);
        harness.addToBattlefield(player1, new DoomsServoGuards());
        DoomsServoGuards returnedCreature = new DoomsServoGuards();
        VillainousSyndication noncreature = new VillainousSyndication();
        DoomsServoGuards opposingCreature = new DoomsServoGuards();
        harness.setLibrary(player1, List.of(returnedCreature));
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(syndication);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Villainous Syndication");
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(returnedCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returnedCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(returnedCreature);
    }

    @Test
    @DisplayName("The enchantment is sacrificed even without a creature card to return")
    void fourthCounterSacrificesWithoutLegalReturnTarget() {
        Permanent syndication = harness.addToBattlefieldAndReturn(player1, new VillainousSyndication());
        syndication.setCounterCount(CounterType.PLAN, 3);
        harness.addToBattlefield(player1, new DoomsServoGuards());
        harness.setLibrary(player1, List.of(new VillainousSyndication()));
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(syndication);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(syndication.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
