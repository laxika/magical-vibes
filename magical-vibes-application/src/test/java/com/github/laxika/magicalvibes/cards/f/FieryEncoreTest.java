package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryEncore.class, GrizzlyBears.class, HillGiant.class, Mountain.class})
class FieryEncoreTest extends BaseCardTest {

    @Test
    @DisplayName("Discards, draws, and damages the target for the discarded nonland's mana value")
    void discardsDrawsAndDamagesForNonlandManaValue() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card discarded = new HillGiant();
        Card drawn = new Mountain();
        Card spell = new FieryEncore();
        harness.setHand(player1, List.of(spell, discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(spell, discarded);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("A land discarded this way is replaced by a draw without dealing damage")
    void landDiscardDoesNotDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card discarded = new Mountain();
        Card drawn = new Mountain();
        harness.setHand(player1, List.of(new FieryEncore(), discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Fiery Encore")
    void stormCopiesForEachPriorSpell() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        harness.setHand(player1, List.of(new FieryEncore(), new HillGiant(), new HillGiant(), new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonplaneswalker permanent")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new FieryEncore()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker");
    }
}
