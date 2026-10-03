package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DesperateMeasures;
import com.github.laxika.magicalvibes.cards.d.Dracogenesis;
import com.github.laxika.magicalvibes.cards.d.DutyBeyondDeath;
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

@CardUsed({BreachingDragonstorm.class, EmrakulTheAeonsTorn.class, Forest.class, GrizzlyBears.class,
        ShivanDragon.class, BoulderbornDragon.class, DesperateMeasures.class, Dracogenesis.class,
        DutyBeyondDeath.class})
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

    @Test
    void emptyLibraryDoesNotOfferACast() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Breaching Dragonstorm");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void allLandLibraryIsExiledWithoutOfferingACast() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Breaching Dragonstorm");
    }

    @Test
    void stopsAtFirstNonlandAndLeavesLaterCardsInLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new BoulderbornDragon(), new Forest()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Boulderborn Dragon");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Forest");
        harness.assertOnBattlefield(player1, "Breaching Dragonstorm");
    }

    @Test
    void spellWithManaValueExactlyEightCanBeCastForFree() {
        harness.setLibrary(player1, List.of(new Dracogenesis()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Dracogenesis"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dracogenesis");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void dragonCastByEnterAbilityReturnsEnchantmentAfterEntering() {
        harness.setLibrary(player1, List.of(new BoulderbornDragon()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Breaching Dragonstorm");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Boulderborn Dragon");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Breaching Dragonstorm");
        harness.assertInHand(player1, "Breaching Dragonstorm");
    }

    @Test
    void opponentDragonDoesNotReturnEnchantment() {
        harness.addToBattlefield(player1, new BreachingDragonstorm());

        harness.enterBattlefieldAndReturn(player2, new BoulderbornDragon());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Breaching Dragonstorm");
        harness.assertNotInHand(player1, "Breaching Dragonstorm");
    }

    @Test
    void nonDragonCreatureDoesNotReturnEnchantment() {
        harness.addToBattlefield(player1, new BreachingDragonstorm());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Breaching Dragonstorm");
        harness.assertNotInHand(player1, "Breaching Dragonstorm");
    }

    @Test
    void acceptedSpellWithNoLegalTargetsGoesToHand() {
        harness.setLibrary(player1, List.of(new DesperateMeasures()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Desperate Measures");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void targetedSpellCanBeCastForFreeWithALegalTarget() {
        harness.addToBattlefield(player2, new BoulderbornDragon());
        harness.setLibrary(player1, List.of(new DesperateMeasures()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Boulderborn Dragon"));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Desperate Measures"));
        assertThat(gd.exiledCards).isEmpty();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Desperate Measures");
        harness.assertOnBattlefield(player2, "Boulderborn Dragon");
        harness.assertOnBattlefield(player1, "Breaching Dragonstorm");
    }

    @Test
    void spellWithUnpayableMandatorySacrificeCannotBeCastForFree() {
        harness.setLibrary(player1, List.of(new DutyBeyondDeath()));
        harness.setHand(player1, List.of(new BreachingDragonstorm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Duty Beyond Death"));
        harness.assertInHand(player1, "Duty Beyond Death");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
