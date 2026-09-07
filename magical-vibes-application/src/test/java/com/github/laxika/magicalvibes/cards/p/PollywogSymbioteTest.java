package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PollywogSymbiote.class, DreamtailHeron.class, Forest.class, GrizzlyBears.class})
class PollywogSymbioteTest extends BaseCardTest {

    @Test
    @DisplayName("Mutate creature spells cost {1} less to cast")
    void mutateCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new PollywogSymbiote());
        harness.setHand(player1, List.of(new DreamtailHeron()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

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
}
