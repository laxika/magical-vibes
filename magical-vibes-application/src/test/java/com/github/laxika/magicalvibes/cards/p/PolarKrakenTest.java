package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PolarKrakenTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private long landsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new PolarKraken()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Island"));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Polar Kraken"));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Polar Kraken"));
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
