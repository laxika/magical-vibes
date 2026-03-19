package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillBottomOfTargetLibraryConditionalTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CellarDoorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Cellar Door has correct activated ability")
    void hasCorrectAbility() {
        CellarDoor card = new CellarDoor();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{3}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(MillBottomOfTargetLibraryConditionalTokenEffect.class);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Cellar Door");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating ability taps Cellar Door")
    void activatingTapsCellarDoor() {
        Permanent cellarDoor = addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(cellarDoor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Bottom card to graveyard =====

    @Test
    @DisplayName("Puts the bottom card of target player's library into their graveyard")
    void putsBottomCardIntoGraveyard() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        while (deck.size() > 5) {
            deck.removeFirst();
        }
        Card bottomCard = deck.getLast();
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bottomCard);
    }

    @Test
    @DisplayName("Takes from the bottom, not the top")
    void takesFromBottomNotTop() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        while (deck.size() > 5) {
            deck.removeFirst();
        }
        Card topCard = deck.getFirst();
        Card bottomCard = deck.getLast();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Top card should still be on top
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(topCard);
        // Bottom card should be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bottomCard);
    }

    @Test
    @DisplayName("Does nothing when target player's library is empty")
    void doesNothingWhenLibraryEmpty() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.getGameData().playerDecks.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // No token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Zombie"));
    }

    // ===== Conditional token creation =====

    @Test
    @DisplayName("Creates a 2/2 black Zombie token when bottom card is a creature")
    void createsZombieTokenWhenCreature() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Put a creature card at the bottom of the library
        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        deck.clear();
        deck.add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zombie")
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2
                        && p.getCard().isToken());
    }

    @Test
    @DisplayName("Does not create a token when bottom card is not a creature")
    void noTokenWhenNotCreature() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Put a non-creature card at the bottom of the library
        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        deck.clear();
        deck.add(new Shock());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Non-creature goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        // No Zombie token created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Zombie"));
    }

    @Test
    @DisplayName("Controller gets the token, not the target player")
    void controllerGetsToken() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Put a creature at the bottom of player2's library
        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        deck.clear();
        deck.add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Controller (player1) gets the token
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zombie"));
        // Target (player2) does NOT get the token
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Zombie"));
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Zombie"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent cellarDoor = addReadyCellarDoor(player1);
        cellarDoor.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Helpers =====

    private Permanent addReadyCellarDoor(Player player) {
        CellarDoor card = new CellarDoor();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
