package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BoundByMoonsilver;
import com.github.laxika.magicalvibes.cards.c.CorruptionOfTowashi;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DelverOfSecrets.class, GrizzlyBears.class, Pyroclasm.class, Shock.class,
        BoundByMoonsilver.class, CorruptionOfTowashi.class})
class DelverOfSecretsTest extends BaseCardTest {

    // ===== Transform when instant on top =====

    @Test
    @DisplayName("Transforms when instant is revealed from top of library")
    void transformsWhenInstantRevealed() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability → queues may ability
        harness.handleMayAbilityChosen(player1, true); // reveal and transform

        assertThat(delver.isTransformed()).isTrue();
        assertThat(delver.getCard().getName()).isEqualTo("Insectile Aberration");
        assertThat(gqs.getEffectivePower(gd, delver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, delver)).isEqualTo(2);
    }

    // ===== Transform when sorcery on top =====

    @Test
    @DisplayName("Transforms when sorcery is revealed from top of library")
    void transformsWhenSorceryRevealed() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());

        Card pyroclasm = new Pyroclasm();
        gd.playerDecks.get(player1.getId()).addFirst(pyroclasm);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability → queues may ability
        harness.handleMayAbilityChosen(player1, true); // reveal and transform

        assertThat(delver.isTransformed()).isTrue();
        assertThat(delver.getCard().getName()).isEqualTo("Insectile Aberration");
    }

    // ===== Decline to reveal =====

    @Test
    @DisplayName("Does not transform when player declines to reveal")
    void doesNotTransformWhenDeclined() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability → queues may ability
        harness.handleMayAbilityChosen(player1, false); // decline to reveal

        assertThat(delver.isTransformed()).isFalse();
        assertThat(delver.getCard().getName()).isEqualTo("Delver of Secrets");
        // Card is still on top of library
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    // ===== Non-instant/sorcery on top =====

    @Test
    @DisplayName("Revealing a creature does not transform")
    void revealingCreatureDoesNotTransform() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability → queues may ability
        harness.handleMayAbilityChosen(player1, true); // reveal — but it's a creature, no transform

        assertThat(delver.isTransformed()).isFalse();
        assertThat(delver.getCard().getName()).isEqualTo("Delver of Secrets");
        // Card is still on top of library
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("Declining to reveal a creature leaves Delver untransformed")
    void decliningToRevealCreature() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability → queues may ability
        harness.handleMayAbilityChosen(player1, false); // decline to reveal

        assertThat(delver.isTransformed()).isFalse();
        assertThat(delver.getCard().getName()).isEqualTo("Delver of Secrets");
        // Card is still on top of library
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Does nothing when library is empty")
    void doesNothingWhenLibraryEmpty() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());

        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability → library empty, nothing happens

        assertThat(delver.isTransformed()).isFalse();
        assertThat(delver.getCard().getName()).isEqualTo("Delver of Secrets");
    }

    // ===== Does not trigger on opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player2);

        // No trigger for player1's Delver during player2's upkeep
        assertThat(delver.isTransformed()).isFalse();
        assertThat(delver.getCard().getName()).isEqualTo("Delver of Secrets");
    }

    @Test
    @DisplayName("Revealing an instant leaves it on top of the library")
    void revealingInstantLeavesLibraryUnchanged() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());
        Card topCard = new Shock();
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(delver.isTransformed()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
    }

    @Test
    @DisplayName("Insectile Aberration does not trigger on later upkeeps")
    void transformedFaceDoesNotTriggerOnLaterUpkeep() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());
        harness.setLibrary(player1, List.of(new Shock(), new Pyroclasm()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(delver.isTransformed()).isTrue();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(delver.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The reveal ability still resolves after Delver is destroyed")
    void revealAbilityResolvesAfterSourceIsDestroyed() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Pyroclasm()));
        harness.setHand(player2, List.of(new Shock()));

        advanceToUpkeep(player1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, delver.getId());
        harness.assertInGraveyard(player1, "Delver of Secrets");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Insectile Aberration");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Bound by Moonsilver prevents transformation after an instant is revealed")
    void cannotTransformWhileBoundByMoonsilver() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new BoundByMoonsilver());
        aura.setAttachedTo(delver.getId());
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Pyroclasm()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(delver.isTransformed()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Transforming Delver triggers Corruption of Towashi")
    void transformationTriggersCorruptionOfTowashi() {
        Permanent delver = harness.addToBattlefieldAndReturn(player1, new DelverOfSecrets());
        harness.addToBattlefield(player1, new CorruptionOfTowashi());
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Pyroclasm()));
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(delver.isTransformed()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

}
