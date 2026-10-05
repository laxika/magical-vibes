package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.d.DreamtailHeron;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PollywogSymbiote.class, DreamtailHeron.class, Forest.class, GrizzlyBears.class, AlmightyBrushwagg.class})
class PollywogSymbioteTest extends BaseCardTest {

    @Test
    @DisplayName("Mutate creature spells cost {1} less to cast")
    void mutateCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new PollywogSymbiote());
        harness.castFromHand(player1, new DreamtailHeron(), "{3}{U}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Dreamtail Heron"));
    }

    @Test
    @DisplayName("Nonmutate creature spells are not reduced")
    void nonmutateCreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new PollywogSymbiote());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a mutate creature draws a card then discards a card")
    void castingMutateCreatureDrawsThenDiscards() {
        harness.addToBattlefield(player1, new PollywogSymbiote());
        harness.setHand(player1, List.of(new DreamtailHeron(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void multipleSymbiotesReduceCostAndTriggerSeparately() {
        harness.addToBattlefield(player1, new PollywogSymbiote());
        harness.addToBattlefield(player1, new PollywogSymbiote());
        harness.setHand(player1, List.of(new DreamtailHeron(), new Forest()));
        harness.setLibrary(player1, List.of(new AlmightyBrushwagg(), new AlmightyBrushwagg()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Almighty Brushwagg");
        harness.assertInHand(player1, "Almighty Brushwagg");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void nonmutateCreatureDoesNotTriggerLooting() {
        harness.addToBattlefield(player1, new PollywogSymbiote());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new AlmightyBrushwagg(), "{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsSymbioteDoesNotReduceCost() {
        harness.addToBattlefield(player2, new PollywogSymbiote());

        assertThatThrownBy(() -> harness.castFromHand(player1, new DreamtailHeron(), "{3}{U}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsSymbioteDoesNotTrigger() {
        harness.addToBattlefield(player2, new PollywogSymbiote());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castFromHand(player1, new DreamtailHeron(), "{4}{U}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void reductionCannotPayColoredMana() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new PollywogSymbiote());
        }

        assertThatThrownBy(() -> harness.castFromHand(player1, new DreamtailHeron(), ""))
                .isInstanceOf(IllegalStateException.class);
    }
}
