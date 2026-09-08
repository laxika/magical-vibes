package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PolarKraken.class, Island.class, BalduvianBears.class})
class PolarKrakenTest extends BaseCardTest {

    private long landsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new PolarKraken(), "{8}{U}{U}{U}");
        harness.passBothPriorities();

        Permanent kraken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Polar Kraken".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(kraken.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying cumulative upkeep sacrifices a land and keeps Polar Kraken")
    void paysCumulativeUpkeep() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new PolarKraken());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(kraken.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kraken);
        assertThat(landsControlledBy(player1.getId())).isEqualTo(0);
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("A nonland permanent cannot pay cumulative upkeep")
    void nonlandCannotPayCumulativeUpkeep() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new PolarKraken());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kraken).contains(bears);
        harness.assertInGraveyard(player1, "Polar Kraken");
    }

    @Test
    @DisplayName("Cumulative upkeep cannot be paid with only part of the required lands")
    void partialCumulativeUpkeepPaymentIsNotAllowed() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new PolarKraken());
        kraken.setCounterCount(CounterType.AGE, 1);
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(kraken.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kraken).contains(island);
        harness.assertInGraveyard(player1, "Polar Kraken");
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent kraken = addCreatureReady(player1, new PolarKraken());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kraken)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 9));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Polar Kraken")
    void declineSacrifices() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new PolarKraken());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kraken);
        harness.assertInGraveyard(player1, "Polar Kraken");
        assertThat(landsControlledBy(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("With no lands, cumulative upkeep auto-sacrifices Polar Kraken")
    void noLandsAutoSacrifices() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new PolarKraken());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kraken);
        harness.assertInGraveyard(player1, "Polar Kraken");
    }

    @Test
    @DisplayName("Cumulative upkeep only considers lands controlled by Polar Kraken's controller")
    void onlyConsidersControllerLands() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new PolarKraken());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kraken);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentIsland);
        harness.assertInGraveyard(player1, "Polar Kraken");
    }

    @Test
    @DisplayName("Second upkeep requires sacrificing two lands")
    void secondUpkeepSacrificesTwoLands() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new PolarKraken());
        harness.addToBattlefield(player1, new Island());

        // First upkeep: exactly one land — auto-sacrificed on accept.
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(kraken.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(landsControlledBy(player1.getId())).isEqualTo(0);

        // Two age counters → need two lands.
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(kraken.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        // Exactly two lands — both auto-sacrificed.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kraken);
        assertThat(landsControlledBy(player1.getId())).isEqualTo(0);
    }
}
