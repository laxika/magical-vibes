package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent crew = addCreatureReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wheel.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, wheel)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addCreatureReady(Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
