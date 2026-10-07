package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BatheInGold;
import com.github.laxika.magicalvibes.cards.o.Owlbear;
import com.github.laxika.magicalvibes.cards.y.YoungRedDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrakkusTheButcher.class, YoungRedDragon.class, BatheInGold.class, Owlbear.class})
class ThrakkusTheButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking doubles the current power of each Dragon you control")
    void attackingDoublesControlledDragonsPower() {
        Permanent thrakkus = addCreatureReady(player1, new ThrakkusTheButcher());
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        Permanent nonDragon = addCreatureReady(player1, new Owlbear());
        Permanent opponentDragon = addCreatureReady(player2, new YoungRedDragon());

        dragon.setPowerModifier(2);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, thrakkus)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, nonDragon)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentDragon)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Dragon power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent thrakkus = addCreatureReady(player1, new ThrakkusTheButcher());
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, thrakkus)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, thrakkus)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger uses Dragons and their power at resolution, then fixes the boost")
    void usesResolutionTimeDragonsAndPower() {
        Permanent thrakkus = addCreatureReady(player1, new ThrakkusTheButcher());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);

        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        dragon.setPowerModifier(2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, thrakkus)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(10);
        dragon.setPowerModifier(dragon.getPowerModifier() + 1);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(11);
        Permanent lateDragon = addCreatureReady(player1, new YoungRedDragon());
        assertThat(gqs.getEffectivePower(gd, lateDragon)).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack trigger still doubles Dragons after Thrakkus leaves")
    void triggerResolvesWithoutThrakkus() {
        Permanent thrakkus = addCreatureReady(player1, new ThrakkusTheButcher());
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(thrakkus);
        gd.playerGraveyards.get(player1.getId()).add(thrakkus.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(6);
    }

    @Test
    @DisplayName("Doubling preserves zero power and makes negative power twice as negative")
    void doublesZeroAndNegativePower() {
        addCreatureReady(player1, new ThrakkusTheButcher());
        Permanent zeroPowerDragon = addCreatureReady(player1, new YoungRedDragon());
        Permanent negativePowerDragon = addCreatureReady(player1, new YoungRedDragon());
        zeroPowerDragon.setPowerModifier(-3);
        negativePowerDragon.setPowerModifier(-5);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, zeroPowerDragon)).isZero();
        assertThat(gqs.getEffectivePower(gd, negativePowerDragon)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, negativePowerDragon)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another Dragon attacking does not trigger Thrakkus")
    void otherDragonAttackingDoesNotTrigger() {
        Permanent thrakkus = addCreatureReady(player1, new ThrakkusTheButcher());
        Permanent dragon = addCreatureReady(player1, new YoungRedDragon());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, thrakkus)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(3);
    }
}
