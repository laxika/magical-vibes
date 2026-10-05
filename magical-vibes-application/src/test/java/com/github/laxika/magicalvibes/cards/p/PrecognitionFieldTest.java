package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SeizeTheSpoils;
import com.github.laxika.magicalvibes.cards.s.ShivanFire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrecognitionField.class, LlanowarElves.class, ShivanFire.class, Divination.class})
class PrecognitionFieldTest extends BaseCardTest {

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
        assertThat(entry.getCard()).isInstanceOf(PrecognitionField.class);
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
        harness.assertOnBattlefield(player1, "Precognition Field");
    }

    @Nested
    @DisplayName("Activated ability: {3}: Exile top card")
    @CardUsed({PrecognitionField.class, LlanowarElves.class, ShivanFire.class, Divination.class})
    class ExileTopCardAbility {

        @Test
        @DisplayName("Exiles top card of controller's library")
        void exilesTopCard() {
            harness.addToBattlefield(player1, new PrecognitionField());
            Card topCard = new LlanowarElves();
            harness.setLibrary(player1, List.of(topCard));
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
            harness.setLibrary(player1, List.of());
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
            Card card1 = new LlanowarElves();
            Card card2 = new ShivanFire();
            harness.setLibrary(player1, List.of(card1, card2));
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
            Card topCard = new LlanowarElves();
            harness.setLibrary(player1, List.of(topCard));
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            Permanent perm = gd.playerBattlefields.get(player1.getId()).getFirst();

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            // Permanent should still be untapped (ability doesn't require tap)
            assertThat(perm.isTapped()).isFalse();
        }
    }

    @Nested
    @DisplayName("Cast instant/sorcery from top of library")
    @CardUsed({PrecognitionField.class, LlanowarElves.class, ShivanFire.class, Divination.class})
    class CastFromLibraryTop {

        @Test
        @DisplayName("Can cast instant from top of library paying its mana cost")
        void castInstantFromLibraryTop() {
            harness.addToBattlefield(player1, new PrecognitionField());
            harness.addToBattlefield(player2, new LlanowarElves());
            Card fire = new ShivanFire();
            harness.setLibrary(player1, List.of(fire));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
            harness.castAndResolveFromLibraryTop(player1, elvesId);

            // Shivan Fire resolved: Llanowar Elves should be dead
            harness.assertNotOnBattlefield(player2, "Llanowar Elves");

            // Shivan Fire should be in graveyard
            harness.assertInGraveyard(player1, "Shivan Fire");

            // Card should no longer be on top of library
            assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(fire);

            // Mana should have been spent
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        }

        @Test
        @DisplayName("Casting from library top increments spells-cast-this-turn")
        void castFromLibraryTopCountsAsSpellCast() {
            harness.addToBattlefield(player1, new PrecognitionField());
            harness.addToBattlefield(player2, new LlanowarElves());
            Card fire = new ShivanFire();
            harness.setLibrary(player1, List.of(fire));
            harness.addMana(player1, ManaColor.RED, 1);
            UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
            harness.castFromLibraryTop(player1, elvesId);

            assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
        }

        @Test
        @DisplayName("Cannot cast creature from top of library")
        void cannotCastCreatureFromTop() {
            harness.addToBattlefield(player1, new PrecognitionField());
            Card elves = new LlanowarElves();
            harness.setLibrary(player1, List.of(elves));
            harness.addMana(player1, ManaColor.GREEN, 1);

            assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                    .isInstanceOf(IllegalStateException.class);

            // Card should still be on top of library
            assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(elves);
        }

        @Test
        @DisplayName("Cannot cast from library top without Precognition Field on battlefield")
        void cannotCastWithoutEffect() {
            // No Precognition Field on battlefield
            Card fire = new ShivanFire();
            harness.setLibrary(player1, List.of(fire));
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @CardUsed({PrecognitionField.class, SeizeTheSpoils.class})
        @DisplayName("Cannot cast a discard-cost spell without a card to discard")
        void cannotCastWhenDiscardCostCannotBePaid() {
            harness.addToBattlefield(player1, new PrecognitionField());
            harness.setHand(player1, List.of());
            Card spoils = new SeizeTheSpoils();
            harness.setLibrary(player1, List.of(spoils));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                    .isInstanceOf(IllegalStateException.class);

            // Rejected atomically: card still on top, mana unspent.
            assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spoils);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        }

        @Test
        @DisplayName("Cannot cast from empty library")
        void cannotCastFromEmptyLibrary() {
            harness.addToBattlefield(player1, new PrecognitionField());
            harness.setLibrary(player1, List.of());
            harness.addMana(player1, ManaColor.RED, 1);

            assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Spell goes on the stack before resolving")
        void spellGoesOnStack() {
            harness.addToBattlefield(player1, new PrecognitionField());
            harness.addToBattlefield(player2, new LlanowarElves());
            Card fire = new ShivanFire();
            harness.setLibrary(player1, List.of(fire));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
            harness.castFromLibraryTop(player1, elvesId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard()).isSameAs(fire);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);

            // Not yet resolved - Elves still alive
            harness.assertOnBattlefield(player2, "Llanowar Elves");
        }
    }

    @Test
    @DisplayName("Cannot cast from library top after Precognition Field leaves the battlefield")
    void cannotCastAfterSourceLeaves() {
        harness.addToBattlefield(player1, new PrecognitionField());
        Card fire = new ShivanFire();
        harness.setLibrary(player1, List.of(fire));
        harness.addMana(player1, ManaColor.RED, 1);

        // Remove Precognition Field
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Precognition Field"));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastSorceryFromTop() {
        harness.addToBattlefield(player1, new PrecognitionField());
        Card firstDraw = new LlanowarElves();
        Card secondDraw = new ShivanFire();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Divination(), firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void topCardIsVisibleOnlyToControllerAndUpdatesAfterExile() {
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Divination()));
        harness.setLibrary(player2, List.of());
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Llanowar Elves"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Divination"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void exileUsesTopCardAtResolutionAfterSourceLeaves() {
        harness.addToBattlefield(player1, new PrecognitionField());
        Card originalTop = new LlanowarElves();
        Card nextTop = new Divination();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(originalTop, nextTop));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(nextTop).doesNotContain(originalTop);
        assertThat(gd.playerHands.get(player1.getId())).contains(originalTop);
    }

    @Test
    void cannotCastSorceryDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new PrecognitionField());
        Card sorcery = new Divination();
        harness.setLibrary(player1, List.of(sorcery));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.activePlayerId = player2.getId();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void cannotCastFromTopWithoutEnoughMana() {
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.addToBattlefield(player2, new LlanowarElves());
        Card fire = new ShivanFire();
        harness.setLibrary(player1, List.of(fire));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1,
                harness.getPermanentId(player2, "Llanowar Elves")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fire);
        assertThat(gd.stack).isEmpty();
    }
}
