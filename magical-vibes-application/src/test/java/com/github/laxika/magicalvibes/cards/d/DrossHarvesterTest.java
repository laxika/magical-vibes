package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AweStrike;
import com.github.laxika.magicalvibes.cards.b.BetrayalOfFlesh;
import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.cards.f.FlayedNim;
import com.github.laxika.magicalvibes.cards.l.LoxodonPeacekeeper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        DrossHarvester.class,
        FlayedNim.class,
        ElectrostaticBolt.class,
        BetrayalOfFlesh.class,
        AweStrike.class,
        LoxodonPeacekeeper.class
})
class DrossHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when another creature dies")
    void gainsLifeWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new DrossHarvester());
        Permanent flayedNim = harness.addToBattlefieldAndReturn(player2, new FlayedNim());
        harness.setLife(player1, 10);

        harness.setHand(player1, List.of(new ElectrostaticBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, flayedNim.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Gains 2 life when Dross Harvester dies")
    void gainsLifeWhenItDies() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new DrossHarvester());
        harness.setLife(player1, 10);

        harness.setHand(player2, List.of(new BetrayalOfFlesh()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(harvester.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Loses 4 life at the beginning of its controller's end step")
    void losesLifeAtEndStep() {
        harness.addToBattlefield(player1, new DrossHarvester());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new DrossHarvester());
        harness.setHand(player1, List.of(new AweStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harvester.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Cannot be blocked by a white creature")
    void cannotBeBlockedByWhiteCreature() {
        addCreatureReady(player1, new DrossHarvester());
        addCreatureReady(player2, new LoxodonPeacekeeper());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Prevents combat damage from white creatures")
    void preventsCombatDamageFromWhiteCreature() {
        Permanent harvester = addCreatureReady(player1, new DrossHarvester());
        addCreatureReady(player2, new LoxodonPeacekeeper());
        harness.setLife(player1, 10);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(harvester.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Dross Harvester");
        harness.assertNotOnBattlefield(player2, "Loxodon Peacekeeper");
        harness.assertLife(player1, 12);
    }
}
