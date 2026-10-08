package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaryWatchdog.class, GrizzlyBears.class, Shock.class})
class WaryWatchdogTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.setHand(player1, List.of(new WaryWatchdog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("When it dies, Wary Watchdog surveils 1")
    void diesWithSurveil() {
        Permanent watchdog = addReadyWatchdog(player1);
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        killWithShock(player2, watchdog.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Another creature's death does not trigger Wary Watchdog")
    void anotherCreatureDeathDoesNotTrigger() {
        addReadyWatchdog(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player1, bears.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An enters trigger can leave the top card in the library")
    void entersCanKeepTopCard() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new WaryWatchdog();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        harness.enterBattlefieldAndReturn(player1, new WaryWatchdog());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A death trigger can leave the top card in the library")
    void diesCanKeepTopCard() {
        Permanent watchdog = addReadyWatchdog(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        killWithShock(player2, watchdog.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(watchdog.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Entering with an empty library finishes without a choice")
    void entersWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent watchdog = harness.enterBattlefieldAndReturn(player1, new WaryWatchdog());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(watchdog);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dying with an empty library finishes without a choice")
    void diesWithEmptyLibrary() {
        Permanent watchdog = addReadyWatchdog(player1);
        harness.setLibrary(player1, List.of());

        killWithShock(player2, watchdog.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(watchdog.getCard());
    }

    @Test
    @DisplayName("Player two's Watchdog surveils player two's library when it dies")
    void opponentWatchdogSurveilsItsControllersLibrary() {
        Permanent watchdog = addReadyWatchdog(player2);
        Card ownTop = new GrizzlyBears();
        Card opponentTop = new GrizzlyBears();
        Card opponentSecond = new WaryWatchdog();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop, opponentSecond));

        killWithShock(player1, watchdog.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownTop, opponentTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentSecond);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentTop, watchdog.getCard());
    }

    private Permanent addReadyWatchdog(Player player) {
        Permanent watchdog = harness.addToBattlefieldAndReturn(player, new WaryWatchdog());
        watchdog.setSummoningSick(false);
        return watchdog;
    }

    private void killWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
