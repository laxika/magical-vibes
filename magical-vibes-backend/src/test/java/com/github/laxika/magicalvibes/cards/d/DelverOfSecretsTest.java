package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InsectileAberration;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealTypeTransformEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DelverOfSecretsTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        DelverOfSecrets card = new DelverOfSecrets();

        // Upkeep transform trigger
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(LookAtTopCardMayRevealTypeTransformEffect.class);
        LookAtTopCardMayRevealTypeTransformEffect effect =
                (LookAtTopCardMayRevealTypeTransformEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(effect.cardTypes()).containsExactlyInAnyOrder(CardType.INSTANT, CardType.SORCERY);

        // Back face exists
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard()).isInstanceOf(InsectileAberration.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("InsectileAberration");
    }

    // ===== Transform when instant on top =====

    @Test
    @DisplayName("Transforms when instant is revealed from top of library")
    void transformsWhenInstantRevealed() {
        harness.addToBattlefield(player1, new DelverOfSecrets());
        Permanent delver = findPermanent(player1, "Delver of Secrets");

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
        harness.addToBattlefield(player1, new DelverOfSecrets());
        Permanent delver = findPermanent(player1, "Delver of Secrets");

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
        harness.addToBattlefield(player1, new DelverOfSecrets());
        Permanent delver = findPermanent(player1, "Delver of Secrets");

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
        harness.addToBattlefield(player1, new DelverOfSecrets());
        Permanent delver = findPermanent(player1, "Delver of Secrets");

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
        harness.addToBattlefield(player1, new DelverOfSecrets());
        Permanent delver = findPermanent(player1, "Delver of Secrets");

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
        harness.addToBattlefield(player1, new DelverOfSecrets());
        Permanent delver = findPermanent(player1, "Delver of Secrets");

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
        harness.addToBattlefield(player1, new DelverOfSecrets());
        Permanent delver = findPermanent(player1, "Delver of Secrets");

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player2);

        // No trigger for player1's Delver during player2's upkeep
        assertThat(delver.isTransformed()).isFalse();
        assertThat(delver.getCard().getName()).isEqualTo("Delver of Secrets");
    }

    // ===== Helpers =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance from untap to upkeep
    }

}
