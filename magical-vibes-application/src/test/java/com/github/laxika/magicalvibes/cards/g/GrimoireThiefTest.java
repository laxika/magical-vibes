package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimoireThief.class, Shock.class, GrizzlyBears.class, TrueBeliever.class})
class GrimoireThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped exiles the top three cards of the opponent's library, tracked with it")
    void becomingTappedExilesTopThreeOfOpponentLibrary() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(thief.getId())).hasSize(3);
        assertThat(gd.exiledCards).filteredOn(e -> thief.getId().equals(e.sourcePermanentId()))
                .allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("Sacrifice ability counters a spell whose name matches an exiled card")
    void sacrificeCountersMatchingSpell() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.addToExile(player2.getId(), new Shock(), thief.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        // Player2 casts Shock at player1; player1 responds with the sacrifice ability.
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20); // Shock was countered, no damage
        harness.assertInGraveyard(player1, "Grimoire Thief");
    }

    @Test
    @DisplayName("Sacrifice ability does not counter a spell whose name is not among exiled cards")
    void sacrificeDoesNotCounterUnmatchedSpell() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.addToExile(player2.getId(), new com.github.laxika.magicalvibes.cards.g.GrizzlyBears(), thief.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities(); // resolves the (empty) counter ability
        harness.passBothPriorities(); // resolves Shock

        harness.assertLife(player1, 18); // Shock resolved for 2 damage
    }

    @Test
    @DisplayName("Sacrifice ability turns all cards exiled with it face up")
    void sacrificeTurnsExiledCardsFaceUp() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        harness.setLibrary(player2, List.of(new Shock(), new GrizzlyBears(), new Shock()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).filteredOn(e -> thief.getId().equals(e.sourcePermanentId()))
                .hasSize(3)
                .allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).filteredOn(e -> thief.getId().equals(e.sourcePermanentId()))
                .hasSize(3)
                .allMatch(e -> !e.faceDown());
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Sacrifice ability counters a matching Battle spell")
    void sacrificeCountersMatchingBattleSpell() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        gd.addToExile(player2.getId(), new InvasionOfZendikar(), thief.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new InvasionOfZendikar(), "{3}{G}");
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType()
                == com.github.laxika.magicalvibes.model.StackEntryType.BATTLE_SPELL);
    }

    @Test
    @DisplayName("The tap trigger cannot exile cards from an opponent with shroud")
    void opponentWithShroudCannotBeTargeted() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        harness.addToBattlefield(player2, new TrueBeliever());
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.getCardsExiledByPermanent(thief.getId())).isEmpty();
    }

    @Test
    @DisplayName("The tap trigger exiles all remaining cards when the library has fewer than three")
    void shortLibraryExilesOnlyRemainingCards() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        Shock first = new Shock();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(thief.getId())).containsExactly(first, second);
        assertThat(gd.exiledCards).filteredOn(e -> thief.getId().equals(e.sourcePermanentId()))
                .hasSize(2)
                .allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Grimoire Thief")
    void anotherCreatureBecomingTappedDoesNotExile() {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(thief.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.getCardsExiledByPermanent(thief.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only Grimoire Thief's controller may look at its face-down exiled cards")
    void controllerCanLookAtExiledCardsButOpponentCannot() throws Exception {
        Permanent thief = addCreatureReady(player1, new GrimoireThief());
        Shock first = new Shock();
        GrizzlyBears second = new GrizzlyBears();
        Shock third = new Shock();
        harness.setLibrary(player2, List.of(first, second, third, new Shock()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage controllerState = mapper.readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        var controllerView = controllerState.battlefields().stream().flatMap(List::stream)
                .filter(permanent -> permanent.id().equals(thief.getId())).findFirst().orElseThrow();
        var opponentView = opponentState.battlefields().stream().flatMap(List::stream)
                .filter(permanent -> permanent.id().equals(thief.getId())).findFirst().orElseThrow();

        assertThat(controllerView.faceDownExiledCards()).extracting(card -> card.id())
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(opponentView.faceDownExiledCards()).isEmpty();
        assertThat(opponentView.faceDownExiledCount()).isEqualTo(3);
    }
}
