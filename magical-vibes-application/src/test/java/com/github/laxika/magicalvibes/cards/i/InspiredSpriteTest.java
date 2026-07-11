package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InspiredSpriteTest extends BaseCardTest {

    private Permanent addTappedSprite() {
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new InspiredSprite());
        sprite.setSummoningSick(false);
        sprite.tap();
        return sprite;
    }

    // ===== Wizard spell cast trigger =====

    @Test
    @DisplayName("Accepting the trigger untaps Inspired Sprite when a Wizard spell is cast")
    void acceptUntaps() {
        Permanent sprite = addTappedSprite();
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(sprite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the trigger leaves Inspired Sprite tapped")
    void declineStaysTapped() {
        Permanent sprite = addTappedSprite();
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(sprite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-Wizard spell does not trigger")
    void nonWizardDoesNotTrigger() {
        addTappedSprite();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== {T}: Draw a card, then discard a card =====

    @Test
    @DisplayName("Activating the ability draws then discards a card")
    void lootAbility() {
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new InspiredSprite());
        sprite.setSummoningSick(false);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Drew a card, now awaiting a discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Net: drew 1, discarded 1 — hand size unchanged, source is tapped
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(sprite.isTapped()).isTrue();
    }
}
