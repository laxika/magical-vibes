package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CellarDoor.class, WalkingCorpse.class, BumpInTheNight.class})
class CellarDoorTest extends BaseCardTest {

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
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Creates a 2/2 black Zombie token when bottom card is a creature")
    void createsZombieTokenWhenCreature() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Put a creature card at the bottom of the library
        harness.setLibrary(player2, List.of(new WalkingCorpse()));

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
        harness.setLibrary(player2, List.of(new BumpInTheNight()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Non-creature goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        // No Zombie token created
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Controller gets the token, not the target player")
    void controllerGetsToken() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Put a creature at the bottom of player2's library
        harness.setLibrary(player2, List.of(new WalkingCorpse()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Controller (player1) gets the token
        harness.assertOnBattlefield(player1, "Zombie");
        // Target (player2) does NOT get the token
        harness.assertNotOnBattlefield(player2, "Zombie");
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player1, List.of(new WalkingCorpse()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Zombie");
    }

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

    @Test
    @DisplayName("Checks the bottom card when the ability resolves")
    void checksBottomCardAtResolution() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card creature = new WalkingCorpse();
        Card sorcery = new BumpInTheNight();
        harness.setLibrary(player2, List.of(creature));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setLibrary(player2, List.of(creature, sorcery));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(sorcery);
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("Resolves after Cellar Door leaves and creates exactly one black Zombie creature")
    void resolvesAfterSourceLeavesBattlefield() {
        addReadyCellarDoor(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card creature = new WalkingCorpse();
        harness.setLibrary(player2, List.of(creature));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Card token = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        assertThat(token.isToken()).isTrue();
        assertThat(token.getName()).isEqualTo("Zombie");
        assertThat(token.hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(token.getPower()).isEqualTo(2);
        assertThat(token.getToughness()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Zombie");
    }

    private Permanent addReadyCellarDoor(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CellarDoor());
        perm.setSummoningSick(false);
        return perm;
    }
}
