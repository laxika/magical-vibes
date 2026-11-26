package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.f.FontOfAgonies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordPointDiplomacy.class, ColossalDreadmaw.class, Forest.class, Island.class,
        PlatinumEmperion.class, FontOfAgonies.class})
class SwordPointDiplomacyTest extends BaseCardTest {
    @Test
    @DisplayName("An opponent whose life total can't change can't pay, so every card goes to the hand")
    void opponentWhoseLifeCannotChangeCannotDenyCards() {
        Card revealed = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(revealed));
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Paying the life for a card is a life payment its abilities see")
    void denyingACardTriggersLifePaymentAbilities() {
        Card revealed = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(revealed));
        var font = harness.addToBattlefieldAndReturn(player2, new FontOfAgonies());
        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealed);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cards below the top three stay in the library")
    void cardsBelowTheTopThreeRemainInLibrary() {
        Card first = new ColossalDreadmaw();
        Card second = new Forest();
        Card third = new Island();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        harness.assertLife(player2, 17);
    }


    @Test
    @DisplayName("Casting and resolving reveals top 3 and presents opponent with choice")
    void castingPresentsOpponentWithChoice() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        Card card3 = new Island();
        harness.setLibrary(player1, List.of(card1, card2, card3));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Opponent denies no cards — all 3 go to controller's hand")
    void opponentDeniesNone() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        Card card3 = new Island();
        harness.setLibrary(player1, List.of(card1, card2, card3));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent pays for no card
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player1, "Colossal Dreadmaw");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        // Opponent life unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent denies one card — pays 3 life, denied card exiled, rest to hand")
    void opponentDeniesOne() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        Card card3 = new Island();
        harness.setLibrary(player1, List.of(card1, card2, card3));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent pays for Colossal Dreadmaw only
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotInHand(player1, "Colossal Dreadmaw");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Colossal Dreadmaw"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Opponent denies all three cards — pays 9 life, all exiled")
    void opponentDeniesAll() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        Card card3 = new Island();
        harness.setLibrary(player1, List.of(card1, card2, card3));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent pays for all three cards
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Opponent with less than 3 life cannot deny any card — all go to hand automatically")
    void opponentCannotAffordToPay() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        Card card3 = new Island();
        harness.setLibrary(player1, List.of(card1, card2, card3));
        harness.setLife(player2, 2);

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // No choice presented — all go to hand automatically
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Colossal Dreadmaw");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent with exactly 3 life can still pay for one card")
    void opponentWithExactly3LifeCanPayForOne() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        Card card3 = new Island();
        harness.setLibrary(player1, List.of(card1, card2, card3));
        harness.setLife(player2, 3);

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Asked (opponent has exactly 3 life, can pay for one); after paying, the other cards aren't asked about
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(0);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Colossal Dreadmaw"));
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Opponent cannot pay for more cards than their life allows")
    void opponentCannotOverpay() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        Card card3 = new Island();
        harness.setLibrary(player1, List.of(card1, card2, card3));
        harness.setLife(player2, 5);

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Paying for the first card leaves 2 life: no payment can be made for the others (CR 119.4), so they aren't
        // asked about
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(card2, card3);
    }

    @Test
    @DisplayName("Empty library does nothing")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fewer than 3 cards in library reveals only what's available")
    void fewerThanThreeCards() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        harness.setLibrary(player1, List.of(card1, card2));

        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // One question for each of the 2 cards
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Opponent pays for none
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each revealed card is its own question; the paid-for card is exiled with the rest")
    void eachRevealedCardIsItsOwnQuestion() {
        Card card1 = new ColossalDreadmaw();
        Card card2 = new Forest();
        Card card3 = new Island();
        harness.setLibrary(player1, List.of(card1, card2, card3));
        castDiplomacy();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(card2, card3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card1);
        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Every opponent is asked about each card in turn order, even after another paid")
    void everyOpponentIsAskedForEachCard() {
        Player player3 = addThirdPlayer();
        Card card1 = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(card1));
        castDiplomacy();

        harness.handleMayAbilityChosen(player2, true);

        // Its ruling: each opponent in turn order chooses whether to pay life for that card.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player3, false);

        harness.assertLife(player2, 17);
        harness.assertLife(player3, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card1);
    }

    private void castDiplomacy() {
        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyard() {
        Card card1 = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(card1));

        SwordPointDiplomacy spd = new SwordPointDiplomacy();
        harness.setHand(player1, List.of(spd));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent makes choice
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Sword-Point Diplomacy");
    }
}
