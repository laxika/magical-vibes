package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PutridLeech;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BantSojourners.class, Terminate.class, PutridLeech.class})
class BantSojournersTest extends BaseCardTest {

    private long soldierTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Soldier".equals(p.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("When it dies, may create a 1/1 white Soldier token")
    void diesCreatesSoldierToken() {
        harness.addToBattlefield(player1, new BantSojourners());

        killWithTerminate();
        harness.passBothPriorities(); // resolve the death trigger -> may-token choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(soldierTokenCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger may create nothing — declining leaves no token")
    void diesMayDeclineToken() {
        harness.addToBattlefield(player1, new BantSojourners());

        killWithTerminate();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(soldierTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Cycling creates a Soldier token and draws a card")
    void cyclingCreatesTokenAndDraws() {
        harness.setHand(player1, List.of(new BantSojourners()));
        harness.setLibrary(player1, List.of(new PutridLeech()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(soldierTokenCount(player1)).isEqualTo(1);
        harness.assertNotInHand(player1, "Putrid Leech");
        harness.passBothPriorities(); // Resolve cycling after the separate token trigger.
        // The cycling draw still happens: Bant Sojourners discarded, the library card drawn.
        harness.assertInGraveyard(player1, "Bant Sojourners");
        harness.assertInHand(player1, "Putrid Leech");
    }

    @Test
    @DisplayName("Cycling may create no token — declining still draws a card")
    void cyclingMayDeclineTokenStillDraws() {
        harness.setHand(player1, List.of(new BantSojourners()));
        harness.setLibrary(player1, List.of(new PutridLeech()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(soldierTokenCount(player1)).isZero();
        harness.assertNotInHand(player1, "Putrid Leech");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Putrid Leech");
    }

    @Test
    @DisplayName("Cycling discards immediately but its token trigger resolves before the draw")
    void cyclingDiscardAndTokenTriggerPrecedeDraw() {
        harness.setHand(player1, List.of(new BantSojourners()));
        harness.setLibrary(player1, List.of(new PutridLeech()));
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Bant Sojourners");
        harness.assertNotInHand(player1, "Bant Sojourners");
        harness.assertNotInHand(player1, "Putrid Leech");
        assertThat(soldierTokenCount(player1)).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(soldierTokenCount(player1)).isEqualTo(1);
        harness.assertNotInHand(player1, "Putrid Leech");
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Soldier"));

        assertThat(soldierTokenCount(player1)).isZero();
        harness.assertNotInHand(player1, "Putrid Leech");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Putrid Leech");
    }

    private void killWithTerminate() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bantId = harness.getPermanentId(player1, "Bant Sojourners");
        harness.castAndResolveInstant(player2, 0, bantId);
    }
}
