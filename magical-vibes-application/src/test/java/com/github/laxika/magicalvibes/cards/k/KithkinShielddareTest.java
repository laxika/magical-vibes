package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KithkinShielddareTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature +2/+2 until end of turn")
    void boostsBlockingCreature() {
        setupShielddare();
        Permanent blocker = addBlockingBear(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(2);
        assertThat(blocker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        setupShielddare();
        Permanent blocker = addBlockingBear(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Taps the Shielddare when activated")
    void tapsOnActivation() {
        setupShielddare();
        Permanent blocker = addBlockingBear(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());

        assertThat(findPermanent(player1, "Kithkin Shielddare").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        setupShielddare();
        Permanent bystander = new Permanent(new GrizzlyBears());
        bystander.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bystander);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    private void setupShielddare() {
        harness.addToBattlefield(player1, new KithkinShielddare());
        findPermanent(player1, "Kithkin Shielddare").setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
    }

    private Permanent addBlockingBear(Player player) {
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        bear.setBlocking(true);
        gd.playerBattlefields.get(player.getId()).add(bear);
        return bear;
    }
}
