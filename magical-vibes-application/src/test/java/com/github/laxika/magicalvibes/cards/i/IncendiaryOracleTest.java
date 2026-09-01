package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncendiaryOracle.class, LlanowarElves.class})
class IncendiaryOracleTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsPowerUntilEndOfTurn() {
        Permanent oracle = addCreatureReady(player1, new IncendiaryOracle());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(oracle.getEffectivePower()).isEqualTo(3);
        assertThat(oracle.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(oracle.getEffectivePower()).isEqualTo(2);
        assertThat(oracle.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void creatureDamagedByIncendiaryOracleIsExiledInsteadOfDying() {
        Permanent oracle = addCreatureReady(player1, new IncendiaryOracle());
        oracle.setAttacking(true);
        addCreatureReady(player2, new LlanowarElves());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Llanowar Elves"));
    }
}
