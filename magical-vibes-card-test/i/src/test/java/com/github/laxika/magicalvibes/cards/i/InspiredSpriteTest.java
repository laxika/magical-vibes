package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forfend;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.s.StonybrookBanneret;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiredSprite.class, StonybrookBanneret.class, Forfend.class, MothdustChangeling.class})
class InspiredSpriteTest extends BaseCardTest {

    private Permanent addTappedSprite() {
        Permanent sprite = addCreatureReady(player1, new InspiredSprite());
        sprite.tap();
        return sprite;
    }

    @Test
    @DisplayName("Accepting the trigger untaps Inspired Sprite when a Wizard spell is cast")
    void acceptUntaps() {
        Permanent sprite = addTappedSprite();
        harness.castFromHand(player1, new StonybrookBanneret(), "{1}{U}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(sprite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the trigger leaves Inspired Sprite tapped")
    void declineStaysTapped() {
        Permanent sprite = addTappedSprite();
        harness.castFromHand(player1, new StonybrookBanneret(), "{1}{U}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(sprite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-Wizard spell does not trigger")
    void nonWizardDoesNotTrigger() {
        Permanent sprite = addTappedSprite();
        harness.castFromHand(player1, new Forfend(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(sprite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a Wizard spell by an opponent does not trigger")
    void opponentWizardDoesNotTrigger() {
        Permanent sprite = addTappedSprite();
        harness.setHand(player2, List.of(new StonybrookBanneret()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(sprite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability draws then discards a card")
    void lootAbility() {
        Permanent sprite = addCreatureReady(player1, new InspiredSprite());
        harness.setHand(player1, List.of(new StonybrookBanneret()));
        harness.setLibrary(player1, List.of(new Forfend()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        // Drew a card, now awaiting a discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Net: drew 1, discarded 1 — hand size unchanged, source is tapped
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(sprite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The newly drawn card can be chosen for discard")
    void canDiscardDrawnCard() {
        Permanent sprite = addCreatureReady(player1, new InspiredSprite());
        StonybrookBanneret retained = new StonybrookBanneret();
        Forfend drawn = new Forfend();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(sprite.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped Sprite cannot activate its looting ability")
    void tappedSpriteCannotLoot() {
        addTappedSprite();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("An untap trigger does not remove summoning sickness")
    void untapDoesNotRemoveSummoningSickness() {
        Permanent sprite = harness.addToBattlefieldAndReturn(player1, new InspiredSprite());
        sprite.tap();
        harness.castFromHand(player1, new StonybrookBanneret(), "{1}{U}");
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(sprite.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Untapping after a Wizard cast permits another loot in the same turn")
    void canLootAgainAfterWizardCast() {
        Permanent sprite = addTappedSprite();
        harness.castFromHand(player1, new StonybrookBanneret(), "{1}{U}");
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.setHand(player1, List.of(new StonybrookBanneret()));
        harness.setLibrary(player1, List.of(new Forfend(), new Forfend()));

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 1);
        assertThat(sprite.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(sprite.isTapped()).isFalse();
        harness.setHand(player1, List.of(new StonybrookBanneret()));

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 1);

        assertThat(sprite.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Forfend).hasSize(2);
    }

    @Test
    @DisplayName("A spell with changeling is a Wizard spell and triggers the untap")
    void changelingSpellTriggersUntap() {
        Permanent sprite = addTappedSprite();
        harness.castFromHand(player1, new MothdustChangeling(), "{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(sprite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Wizard entering without being cast does not trigger the untap")
    void wizardEnteringWithoutCastDoesNotTrigger() {
        Permanent sprite = addTappedSprite();

        harness.enterBattlefieldAndReturn(player1, new StonybrookBanneret());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(sprite.isTapped()).isTrue();
    }
}
