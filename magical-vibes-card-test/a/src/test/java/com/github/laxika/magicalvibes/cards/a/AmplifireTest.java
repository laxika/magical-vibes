package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Maro;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Amplifire.class, Shock.class, GrizzlyBears.class, Maro.class})
class AmplifireTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals until a creature and sets Amplifire's base power and toughness to twice its P/T")
    void revealsUntilCreatureAndSetsBasePowerToughness() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, bears));

        advanceToUpkeepAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(shock, bears);
    }

    @Test
    @DisplayName("Returns the whole revealed library to the bottom when no creature is found")
    void noCreatureSetsBasePowerToughnessToZero() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

        advanceToUpkeepAndResolve(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amplifire);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(amplifire.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("An empty library makes Amplifire a 0/0 and it dies")
    void emptyLibrarySetsBasePowerToughnessToZero() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        harness.setLibrary(player1, List.of());

        advanceToUpkeepAndResolve(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amplifire);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(amplifire.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only revealed cards go to the bottom, leaving unrevealed cards in order")
    void leavesUnrevealedCardsOnTop() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        Card revealedShock = new Shock();
        Card bears = new GrizzlyBears();
        Card unrevealedShock = new Shock();
        Card maro = new Maro();
        harness.setLibrary(player1, List.of(revealedShock, bears, unrevealedShock, maro));

        advanceToUpkeepAndResolve(player1);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.subList(0, 2)).containsExactly(unrevealedShock, maro);
        assertThat(library.subList(2, library.size())).containsExactlyInAnyOrder(revealedShock, bears);
        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(4);
    }

    @Test
    @DisplayName("The revealed creature's power and toughness are fixed at resolution")
    void characteristicDefiningValuesAreNotUpdatedAfterResolution() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Maro()));

        advanceToUpkeepAndResolve(player1);
        harness.setHand(player1, List.of(new Shock()));

        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(6);
    }

    @Test
    @DisplayName("The reveal still happens if Amplifire leaves before its trigger resolves")
    void revealsAfterSourceLeavesBattlefield() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, shock));

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(amplifire);
        gd.playerGraveyards.get(player1.getId()).add(amplifire.getCard());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amplifire);
    }

    @Test
    @DisplayName("Amplifire does not reveal cards during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(bears, shock));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, shock);
        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses a creature card's characteristic-defining power and toughness")
    void usesCharacteristicDefiningPowerToughness() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Maro()));

        advanceToUpkeepAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(6);
    }

    @Test
    @DisplayName("The base power and toughness setting expires at the beginning of your next turn")
    void basePowerToughnessExpiresAtNextTurn() {
        Permanent amplifire = addCreatureReady(player1, new Amplifire());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeepAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(4);

        gd.playerDecks.get(player1.getId()).clear();
        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(4);

        endTurn(player2);
        assertThat(gqs.getEffectivePower(gd, amplifire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, amplifire)).isEqualTo(1);
    }

    private void advanceToUpkeepAndResolve(Player player) {
        advanceToUpkeep(player);
        harness.passBothPriorities();
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer == player1 ? player2 : player1, TurnStep.UPKEEP);
    }
}
