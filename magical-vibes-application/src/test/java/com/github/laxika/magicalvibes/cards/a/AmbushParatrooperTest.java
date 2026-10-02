package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.y.YotianFrontliner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbushParatrooper.class, YotianFrontliner.class})
class AmbushParatrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Ability boosts creatures you control until end of turn")
    void boostsOwnCreaturesUntilEndOfTurn() {
        Permanent paratrooper = harness.addToBattlefieldAndReturn(player1, new AmbushParatrooper());
        Permanent ownFrontliner = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        Permanent opponentFrontliner = harness.addToBattlefieldAndReturn(player2, new YotianFrontliner());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paratrooper)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paratrooper)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownFrontliner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownFrontliner)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentFrontliner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentFrontliner)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownFrontliner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownFrontliner)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, paratrooper)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, paratrooper)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Paratrooper can activate repeatedly")
    void repeatedActivationsStackWithoutTapping() {
        Permanent paratrooper = harness.addToBattlefieldAndReturn(player1, new AmbushParatrooper());
        paratrooper.setTapped(true);
        paratrooper.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paratrooper)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, paratrooper)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost includes creatures present at resolution but not later arrivals")
    void determinesAffectedCreaturesAtResolution() {
        harness.addToBattlefield(player1, new AmbushParatrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step")
    void canCastDuringOpponentsEndStep() {
        harness.setHand(player1, List.of(new AmbushParatrooper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ambush Paratrooper");
        harness.assertNotInHand(player1, "Ambush Paratrooper");
    }
}
