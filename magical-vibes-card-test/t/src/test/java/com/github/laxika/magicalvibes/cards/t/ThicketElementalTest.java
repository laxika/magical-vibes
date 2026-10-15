package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThicketElemental.class, Forest.class, RagingKavu.class})
class ThicketElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without the kicked ability when not kicked")
    void withoutKickerDoesNotTrigger() {
        harness.setLibrary(player1, List.of(new RagingKavu()));
        harness.castFromHand(player1, new ThicketElemental(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Raging Kavu");
    }

    @Test
    @DisplayName("When kicked, may put the first revealed creature onto the battlefield")
    void kickedMayRevealCreatureOntoBattlefield() {
        harness.setHand(player1, List.of(new ThicketElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new RagingKavu(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Raging Kavu");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Forest", "Forest");
    }

    @Test
    @DisplayName("If no creature is revealed, all revealed cards return to the library")
    void kickedWithNoCreatureReturnsRevealedCards() {
        harness.setHand(player1, List.of(new ThicketElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Forest");
    }

    @Test
    @DisplayName("With an empty library, the accepted kicked ability reveals nothing")
    void kickedWithEmptyLibraryRevealsNothing() {
        harness.setHand(player1, List.of(new ThicketElemental()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Stops at the first creature and a revealed Thicket Elemental was not kicked")
    void revealedThicketElementalDoesNotTriggerKickerAbility() {
        ThicketElemental revealedCreature = new ThicketElemental();
        RagingKavu remainingCreature = new RagingKavu();
        harness.setHand(player1, List.of(new ThicketElemental()));
        harness.setLibrary(player1, List.of(revealedCreature, remainingCreature, new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(revealedCreature.getId()));
        harness.assertNotOnBattlefield(player1, "Raging Kavu");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Raging Kavu", "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the kicked ability leaves the library unchanged")
    void kickedAbilityMayBeDeclined() {
        harness.setHand(player1, List.of(new ThicketElemental()));
        harness.setLibrary(player1, List.of(new Forest(), new RagingKavu()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Raging Kavu");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Raging Kavu");
    }
}
