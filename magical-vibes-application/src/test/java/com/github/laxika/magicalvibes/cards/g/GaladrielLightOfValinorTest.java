package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaladrielLightOfValinor.class, GrizzlyBears.class})
class GaladrielLightOfValinorTest extends BaseCardTest {

    private static final String MANA = "Add {G}{G}{G}.";
    private static final String COUNTERS = "Put a +1/+1 counter on each creature you control.";
    private static final String SCRY = "Scry 2, then draw a card.";

    @Test
    @DisplayName("Mana mode adds three green mana")
    void manaMode() {
        harness.addToBattlefield(player1, new GaladrielLightOfValinor());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        triggerWithCreature(MANA);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on each creature you control")
    void counterMode() {
        harness.addToBattlefield(player1, new GaladrielLightOfValinor());
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        triggerWithCreature(COUNTERS);

        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .allMatch(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 1);
    }

    @Test
    @DisplayName("Scry mode scries two and then draws a card")
    void scryMode() {
        harness.addToBattlefield(player1, new GaladrielLightOfValinor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Card first = new GrizzlyBears();
        Card second = new GaladrielLightOfValinor();
        harness.setLibrary(player1, List.of(first, second));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        triggerWithCreature(SCRY);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerHands.get(player1.getId()).getLast()).isSameAs(first);
    }

    @Test
    @DisplayName("Each mode can be chosen only once per turn")
    void modesAreConsumedForTheTurn() {
        harness.addToBattlefield(player1, new GaladrielLightOfValinor());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        triggerWithCreature(MANA);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, MANA))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, COUNTERS);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Galadriel does not trigger for its own entry or an opponent's creature")
    void onlyTriggersForAnotherCreatureYouControl() {
        harness.setHand(player1, List.of(new GaladrielLightOfValinor()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void triggerWithCreature(String mode) {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
    }
}
