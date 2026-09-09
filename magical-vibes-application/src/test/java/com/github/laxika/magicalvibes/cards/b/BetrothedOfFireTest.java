package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BetrothedOfFire.class, BenalishInfantry.class, BenalishKnight.class})
class BetrothedOfFireTest extends BaseCardTest {

    private Permanent attach(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BetrothedOfFire());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    @DisplayName("Sacrificing an untapped creature gives the enchanted creature +2/+0")
    void sacrificeUntappedCreatureBoostsEnchanted() {
        Permanent bears = addCreatureReady(player1, new BenalishKnight());
        bears.tap();
        attach(bears);
        addCreatureReady(player1, new BenalishInfantry());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Benalish Infantry");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new BenalishKnight());
        bears.tap();
        attach(bears);
        addCreatureReady(player1, new BenalishInfantry());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the first ability with no untapped creature to sacrifice")
    void cannotSacrificeWhenAllCreaturesTapped() {
        Permanent bears = addCreatureReady(player1, new BenalishKnight());
        bears.tap();
        attach(bears);
        Permanent elves = addCreatureReady(player1, new BenalishInfantry());
        elves.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("Sacrificing the enchanted creature gives creatures you control +2/+0")
    void sacrificeEnchantedBoostsTeam() {
        Permanent bears = addCreatureReady(player1, new BenalishKnight());
        attach(bears);
        Permanent elves = addCreatureReady(player1, new BenalishInfantry());
        Permanent opponentBears = addCreatureReady(player2, new BenalishKnight());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Benalish Knight");
        harness.assertNotOnBattlefield(player1, "Betrothed of Fire");
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice enchanted creature is paid when the second ability is activated")
    void sacrificeEnchantedCreatureIsActivationCost() {
        Permanent creature = addCreatureReady(player1, new BenalishKnight());
        attach(creature);

        harness.activateAbility(player1, 1, 1, null, null);

        harness.assertInGraveyard(player1, "Benalish Knight");
    }
}
