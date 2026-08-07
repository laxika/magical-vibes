package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Valor in Akros")
class ValorInAkrosTest extends BaseCardTest {

    private void castBears(Player player) {
        harness.setHand(player, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature you control entering pumps your team")
    void ownCreatureEnterPumpsTeam() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBears(player1);
        harness.passBothPriorities(); // resolve the trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent entered = battlefield.get(battlefield.size() - 1);

        assertThat(existing.getPowerModifier()).isEqualTo(1);
        assertThat(existing.getToughnessModifier()).isEqualTo(1);
        assertThat(entered.getPowerModifier()).isEqualTo(1);
        assertThat(entered.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBears(player1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(existing.getPowerModifier()).isEqualTo(0);
        assertThat(existing.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ValorInAkros());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        castBears(player2);
        harness.passBothPriorities();

        assertThat(existing.getPowerModifier()).isEqualTo(0);
        assertThat(existing.getToughnessModifier()).isEqualTo(0);
    }
}
