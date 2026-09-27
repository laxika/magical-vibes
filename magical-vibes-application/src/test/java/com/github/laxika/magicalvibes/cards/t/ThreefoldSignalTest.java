package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SproutingThrinax;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreefoldSignal.class, Forest.class, GrizzlyBears.class, SproutingThrinax.class})
class ThreefoldSignalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a scry 3 trigger")
    void entersWithScryThree() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card next = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, next));
        harness.setHand(player1, List.of(new ThreefoldSignal()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, next, first, third);
    }

    @Test
    @DisplayName("Gives exactly three-color spells replicate {3}")
    void givesExactlyThreeColorSpellsReplicateThree() {
        harness.addToBattlefield(player1, new ThreefoldSignal());
        harness.setHand(player1, List.of(new SproutingThrinax()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{3}"));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sprouting Thrinax")).hasSize(2);
        assertThat(findPermanents(player1, "Sprouting Thrinax"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Does not give replicate to spells with another number of colors")
    void doesNotGiveReplicateToNonThreeColorSpells() {
        harness.addToBattlefield(player1, new ThreefoldSignal());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }
}
