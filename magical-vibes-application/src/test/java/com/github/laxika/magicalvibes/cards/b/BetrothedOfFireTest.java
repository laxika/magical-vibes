package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BetrothedOfFireTest extends BaseCardTest {

    private Permanent attach(Permanent host) {
        Permanent aura = new Permanent(new BetrothedOfFire());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Sacrificing an untapped creature gives the enchanted creature +2/+0")
    void sacrificeUntappedCreatureBoostsEnchanted() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        attach(bears);
        addCreatureReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+0 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        attach(bears);
        addCreatureReady(player1, new LlanowarElves());

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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        attach(bears);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        elves.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Sacrificing the enchanted creature gives creatures you control +2/+0")
    void sacrificeEnchantedBoostsTeam() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attach(bears);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Betrothed of Fire");
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
    }
}
