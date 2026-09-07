package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Calciderm.class, Shock.class})
class CalcidermTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four time counters")
    void entersWithTimeCounters() {
        harness.setHand(player1, List.of(new Calciderm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent calciderm = findPermanent(player1, "Calciderm");

        assertThat(calciderm.getCounterCount(CounterType.TIME)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removes one time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());
        calciderm.setCounterCount(CounterType.TIME, 4);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(calciderm.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(calciderm);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());
        calciderm.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Calciderm");
        harness.assertInGraveyard(player1, "Calciderm");
    }

    @Test
    @DisplayName("Does not sacrifice again when it has no time counters")
    void noTimeCountersDoesNotTriggerSacrifice() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());
        calciderm.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(calciderm);
    }

    @Test
    @DisplayName("Cannot be targeted by spells because it has shroud")
    void cannotBeTargetedBySpells() {
        Permanent calciderm = addCreatureReady(player1, new Calciderm());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, calciderm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
