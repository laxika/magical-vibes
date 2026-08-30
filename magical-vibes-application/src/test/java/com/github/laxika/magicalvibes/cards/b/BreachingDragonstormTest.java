package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EmrakulTheAeonsTorn;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreachingDragonstorm.class, EmrakulTheAeonsTorn.class, Forest.class, GrizzlyBears.class, ShivanDragon.class})
class BreachingDragonstormTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it exiles until a nonland and offers a spell with mana value 8 or less")
    void exilesUntilEligibleNonlandAndOffersFreeCast() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Can cast the eligible nonland card without paying its mana cost")
    void acceptsEligibleNonlandForFree() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("A nonland card with mana value greater than 8 goes to hand")
    void highManaValueCardGoesToHand() {
        harness.setLibrary(player1, List.of(new Forest(), new EmrakulTheAeonsTorn()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Emrakul, the Aeons Torn");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Returns to its owner's hand when a Dragon you control enters")
    void returnsWhenAllyDragonEnters() {
        harness.addToBattlefield(player1, new BreachingDragonstorm());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Breaching Dragonstorm");
        harness.assertInHand(player1, "Breaching Dragonstorm");
    }
}
