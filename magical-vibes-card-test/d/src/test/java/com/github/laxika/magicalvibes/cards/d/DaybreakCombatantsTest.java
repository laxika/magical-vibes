package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaybreakCombatants.class, GrizzlyBears.class})
class DaybreakCombatantsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +2/+0 until end of turn")
    void etbBoostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDaybreakCombatants(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDaybreakCombatants(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if the target creature leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDaybreakCombatants(bears.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can be cast onto an empty battlefield and target itself with its ETB")
    void castWithoutTarget() {
        harness.castFromHand(player1, new DaybreakCombatants(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Daybreak Combatants");
        Permanent combatants = findPermanent(player1, "Daybreak Combatants");
        harness.handlePermanentChosen(player1, combatants.getId());
        harness.passBothPriorities();

        assertThat(combatants.getEffectivePower()).isEqualTo(4);
        assertThat(combatants.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can attack the turn it enters with its self-targeted boost")
    void canAttackImmediately() {
        harness.castFromHand(player1, new DaybreakCombatants(), "{2}{R}");
        harness.passBothPriorities();
        Permanent combatants = findPermanent(player1, "Daybreak Combatants");
        harness.handlePermanentChosen(player1, combatants.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(combatants.isTapped()).isTrue();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("ETB still boosts its target if Daybreak Combatants leaves before resolution")
    void etbResolvesAfterSourceLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDaybreakCombatants(bears.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private void castDaybreakCombatants(UUID targetId) {
        harness.setHand(player1, List.of(new DaybreakCombatants()));
        addManaForDaybreakCombatants();
        harness.castCreature(player1, 0, List.of(targetId));
    }

    private void addManaForDaybreakCombatants() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
