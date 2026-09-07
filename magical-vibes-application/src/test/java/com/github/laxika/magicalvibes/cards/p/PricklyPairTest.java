package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PricklyPair.class, GrizzlyBears.class})
class PricklyPairTest extends BaseCardTest {

    @Test
    @DisplayName("When Prickly Pair enters, it creates a Mercenary token")
    void enterTheBattlefieldCreatesMercenaryToken() {
        castPricklyPair();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    @DisplayName("The Mercenary token boosts a creature you control")
    void mercenaryBoostsCreatureYouControl() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castPricklyPair();

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        prepareSorcerySpeedActivation();

        harness.activateAbility(player1, mercenaryIndex(mercenary), 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Mercenary token cannot target an opposing creature")
    void mercenaryCannotTargetOpposingCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        castPricklyPair();

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        prepareSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex(mercenary), 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("The Mercenary can only be activated at sorcery speed")
    void mercenaryRequiresSorcerySpeed() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castPricklyPair();

        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex(mercenary), 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void castPricklyPair() {
        harness.setHand(player1, List.of(new PricklyPair()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int mercenaryIndex(Permanent mercenary) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
    }
}
