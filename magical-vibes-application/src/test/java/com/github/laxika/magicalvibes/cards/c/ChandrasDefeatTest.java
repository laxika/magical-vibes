package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandrasDefeat.class, ChandraBoldPyromancer.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, NicolBolasGodPharaoh.class})
class ChandrasDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a red creature and does not loot")
    void dealsDamageToRedCreatureNoLoot() {
        Permanent redCreature = addCreatureReady(player2, new HillGiant());

        Card keeper = new GrizzlyBears();
        harness.setHand(player1, List.of(new ChandrasDefeat(), keeper));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, redCreature.getId());

        // Hill Giant (3/3) took 5 damage and died.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(redCreature.getId()));

        // Not a Chandra planeswalker → no loot: no prompt, hand unchanged, nothing drawn.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to a Chandra planeswalker, then loots on accept")
    void dealsDamageToChandraAndLoots() {
        // Chandra, Bold Pyromancer is a red Chandra planeswalker.
        Permanent chandra = addCreatureReady(player2, new ChandraBoldPyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 10);

        harness.setLibrary(player1, List.of(new Forest()));
        Card toDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(new ChandrasDefeat(), toDiscard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, chandra.getId());

        // 5 damage removed 5 loyalty.
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        // Chandra planeswalker → may discard a card, and if you do, draw a card.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Discard happens before the draw (rummage).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Declining the loot on a Chandra planeswalker discards and draws nothing")
    void chandraLootDecline() {
        Permanent chandra = addCreatureReady(player2, new ChandraBoldPyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 10);

        Card keeper = new GrizzlyBears();
        harness.setHand(player1, List.of(new ChandrasDefeat(), keeper));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, chandra.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        // No discard, no draw.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-red creature")
    void cannotTargetNonRedCreature() {
        // A legal red target exists, so the spell is castable; the green creature is an illegal choice.
        Permanent red = addCreatureReady(player2, new HillGiant());
        Permanent green = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ChandrasDefeat()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, green.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }

    @Test
    @DisplayName("May rummage even when the damage removes Chandra's last loyalty")
    void lethalDamageToChandraStillAllowsRummage() {
        harness.setHand(player2, List.of());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ChandrasDefeat(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, chandra.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player2, "Chandra, Bold Pyromancer");
        harness.assertNotOnBattlefield(player2, "Chandra, Bold Pyromancer");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting with an empty hand cannot draw a card")
    void emptyHandCannotRummage() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 10);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new ChandrasDefeat()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, chandra.getId());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("A departed Chandra target prevents both damage and rummaging")
    void departedTargetDoesNotAllowRummage() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 10);
        Card keeper = new GrizzlyBears();
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new ChandrasDefeat(), keeper));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, chandra.getId());
        gd.playerBattlefields.get(player2.getId()).remove(chandra);
        harness.setExile(player2, List.of(chandra.getCard()));
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(10);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keeper);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Chandra's Defeat");
    }

    @Test
    @DisplayName("A multicolored red planeswalker without the Chandra subtype does not allow rummaging")
    void redNonChandraPlaneswalkerDoesNotAllowRummage() {
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 7);
        Card keeper = new Forest();
        harness.setHand(player1, List.of(new ChandrasDefeat(), keeper));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bolas.getId());

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keeper);
    }
}
