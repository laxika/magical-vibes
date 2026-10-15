package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BigWheel.class, Forest.class, GrizzlyBears.class})
class BigWheelTest extends BaseCardTest {

    @Test
    @DisplayName("When Big Wheel enters, accepting may discards then draws a card")
    void acceptMayDiscardsThenDraws() {
        Card discardedCard = new GrizzlyBears();
        Card drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, new ArrayList<>(List.of(new BigWheel(), discardedCard)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isSameAs(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isSameAs(discardedCard);
    }

    @Test
    @DisplayName("Declining Big Wheel's may ability does not discard or draw")
    void declineMayDoesNothing() {
        Card discardedCard = new GrizzlyBears();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, new ArrayList<>(List.of(new BigWheel(), discardedCard)));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Crew 2 animates Big Wheel")
    void crewAnimatesVehicle() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new BigWheel());
        wheel.setSummoningSick(false);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wheel.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, wheel)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, wheel)).isFalse();
    }

    @Test
    @DisplayName("Accepting with an empty hand does not draw a card")
    void emptyHandDoesNotDraw() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new BigWheel(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew a summoning-sick Vehicle")
    void summoningSickCreaturesCanCrew() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new BigWheel());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        wheel.setSummoningSick(true);
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, wheel)).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures cannot pay the crew cost")
    void tappedCreatureCannotCrew() {
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new BigWheel());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, wheel)).isFalse();
    }

    @Test
    @DisplayName("A crewed Big Wheel tramples over a blocker")
    void crewedVehicleDealsExcessCombatDamage() {
        Permanent wheel = addCreatureReady(player1, new BigWheel());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        wheel.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Big Wheel");
    }

}
