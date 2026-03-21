package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrecognitionFieldTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct static effects and activated ability")
    void hasCorrectEffects() {
        PrecognitionField card = new PrecognitionField();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(LookAtTopCardOfOwnLibraryEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC).get(1)).isInstanceOf(AllowCastFromTopOfLibraryEffect.class);

        AllowCastFromTopOfLibraryEffect castEffect = (AllowCastFromTopOfLibraryEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(castEffect.castableTypes()).containsExactlyInAnyOrder(CardType.INSTANT, CardType.SORCERY);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}");
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(ExileTopCardOfOwnLibraryEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts Precognition Field on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PrecognitionField()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Precognition Field");
    }

    @Test
    @DisplayName("Resolving puts Precognition Field on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PrecognitionField()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Precognition Field"));
    }

    // ===== Activated ability: {3}: Exile the top card of your library =====

    @Nested
    @DisplayName("Activated ability: {3}: Exile top card")
    class ExileTopCardAbility {

        @Test
        @DisplayName("Exiles top card of controller's library")
        void exilesTopCard() {
            harness.addToBattlefield(player1, new PrecognitionField());
            Card topCard = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).addFirst(topCard);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            // Precognition Field is at index 0
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
            assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        }

        @Test
        @DisplayName("Does nothing when library is empty")
        void emptyLibraryDoesNothing() {
            harness.addToBattlefield(player1, new PrecognitionField());
            gd.playerDecks.get(player1.getId()).clear();
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            // No error, game continues normally
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        }

        @Test
        @DisplayName("Can be activated multiple times per turn")
        void canActivateMultipleTimes() {
            harness.addToBattlefield(player1, new PrecognitionField());
            Card card1 = new GrizzlyBears();
            Card card2 = new Shock();
            gd.playerDecks.get(player1.getId()).addFirst(card2);
            gd.playerDecks.get(player1.getId()).addFirst(card1);
            harness.addMana(player1, ManaColor.COLORLESS, 6);

            // First activation
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card1);

            // Second activation
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card1, card2);
        }

        @Test
        @DisplayName("Does not require tapping")
        void doesNotRequireTap() {
            harness.addToBattlefield(player1, new PrecognitionField());
            Card topCard = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).addFirst(topCard);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            Permanent perm = gd.playerBattlefields.get(player1.getId()).getFirst();

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            // Permanent should still be untapped (ability doesn't require tap)
            assertThat(perm.isTapped()).isFalse();
        }
    }

    // ===== Cast from top of library =====

    @Nested
    @DisplayName("Cast instant/sorcery from top of library")
    class CastFromLibraryTop {

        @Test
        @DisplayName("Can cast instant from top of library paying its mana cost")
        void castInstantFromLibraryTop() {
            harness.addToBattlefield(player1, new PrecognitionField());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Card shock = new Shock();
            gd.playerDecks.get(player1.getId()).addFirst(shock);
            harness.addMana(player1, ManaColor.RED, 1);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castAndResolveFromLibraryTop(player1, bearsId);

            // Shock resolved: Grizzly Bears should be dead
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

            // Shock should be in graveyard
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Shock"));

            // Card should no longer be on top of library
            assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(shock);

            // Mana should have been spent
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Casting from library top increments spells-cast-this-turn")
        void castFromLibraryTopCountsAsSpellCast() {
            harness.addToBattlefield(player1, new PrecognitionField());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Card shock = new Shock();
            gd.playerDecks.get(player1.getId()).addFirst(shock);
            harness.addMana(player1, ManaColor.RED, 1);
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castFromLibraryTop(player1, bearsId);

            assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
        }

        @Test
        @DisplayName("Cannot cast creature from top of library")
        void cannotCastCreatureFromTop() {
            harness.addToBattlefield(player1, new PrecognitionField());
            Card bears = new GrizzlyBears();
            gd.playerDecks.get(player1.getId()).addFirst(bears);
            harness.addMana(player1, ManaColor.GREEN, 2);

            assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                    .isInstanceOf(IllegalStateException.class);

            // Card should still be on top of library
            assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
        }

        @Test
        @DisplayName("Cannot cast from library top without Precognition Field on battlefield")
        void cannotCastWithoutEffect() {
            // No Precognition Field on battlefield
            Card shock = new Shock();
            gd.playerDecks.get(player1.getId()).addFirst(shock);
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot cast from empty library")
        void cannotCastFromEmptyLibrary() {
            harness.addToBattlefield(player1, new PrecognitionField());
            gd.playerDecks.get(player1.getId()).clear();
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Spell goes on the stack before resolving")
        void spellGoesOnStack() {
            harness.addToBattlefield(player1, new PrecognitionField());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Card shock = new Shock();
            gd.playerDecks.get(player1.getId()).addFirst(shock);
            harness.addMana(player1, ManaColor.RED, 1);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castFromLibraryTop(player1, bearsId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard()).isSameAs(shock);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);

            // Not yet resolved - Bears still alive
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }
    }

    // ===== Effect removed when source leaves =====

    @Test
    @DisplayName("Cannot cast from library top after Precognition Field leaves the battlefield")
    void cannotCastAfterSourceLeaves() {
        harness.addToBattlefield(player1, new PrecognitionField());
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);
        harness.addMana(player1, ManaColor.RED, 1);

        // Remove Precognition Field
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Precognition Field"));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }
}
