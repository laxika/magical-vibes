package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Antagonize;
import com.github.laxika.magicalvibes.cards.g.GiftOfFangs;
import com.github.laxika.magicalvibes.cards.g.Goldhound;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevilishValet.class, Goldhound.class, Antagonize.class, GiftOfFangs.class})
class DevilishValetTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles its power when another creature you control enters")
    void doublesPowerWhenAllyCreatureEnters() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        harness.castFromHand(player1, new Goldhound(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valet)).isEqualTo(3);
    }

    @Test
    @DisplayName("Doubles its current power for each entering creature")
    void doublesCurrentPowerForEachAllyCreature() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        harness.castFromHand(player1, new Goldhound(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromHand(player1, new Goldhound(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void doesNotTriggerForOpponentCreature() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Goldhound(), "{R}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(1);
    }

    @Test
    @DisplayName("The power doubling wears off at end of turn")
    void powerDoublingWearsOffAtEndOfTurn() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        harness.castFromHand(player1, new Goldhound(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering does not trigger its own alliance ability")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new DevilishValet(), "{2}{R}");
        harness.passBothPriorities();

        Permanent valet = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(1);
    }

    @Test
    @DisplayName("Another Valet entering doubles only the existing Valet")
    void anotherValetTriggersOnlyExistingValet() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DevilishValet());

        harness.castFromHand(player1, new DevilishValet(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubling uses power at resolution, including a pump in response")
    void doublesPowerAtResolution() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());
        harness.castFromHand(player1, new Goldhound(), "{R}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Antagonize()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, valet.getId());
        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(5);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, valet)).isEqualTo(6);
    }

    @Test
    @DisplayName("Negative power is doubled rather than treated as zero")
    void doublesNegativePower() {
        Permanent valet = harness.addToBattlefieldAndReturn(player1, new DevilishValet());
        harness.setHand(player1, List.of(new GiftOfFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, valet.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(-1);

        harness.castFromHand(player1, new Goldhound(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valet)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, valet)).isEqualTo(1);
    }
}
