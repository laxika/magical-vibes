package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BoggartForager;
import com.github.laxika.magicalvibes.cards.s.SowerOfTemptation;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LowlandOaf.class, BoggartForager.class, LeafGilder.class, SowerOfTemptation.class})
class LowlandOafTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +1/+0 and flying to a target Goblin you control")
    void pumpsAndGrantsFlying() {
        Permanent oaf = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        oaf.setSummoningSick(false);

        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartForager());

        int basePower = gqs.getEffectivePower(gd, goblin);
        int baseToughness = gqs.getEffectiveToughness(gd, goblin);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, goblin.getId());
        assertThat(gqs.hasKeyword(gd, after, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
        assertThat(oaf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Target Goblin is sacrificed at the beginning of the next end step")
    void sacrificesTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent oaf = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        oaf.setSummoningSick(false);

        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartForager());

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        // Still on the battlefield during the main phase.
        harness.assertOnBattlefield(player1, "Boggart Forager");

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Boggart Forager");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boggart Forager");
        harness.assertInGraveyard(player1, "Boggart Forager");
    }

    @Test
    @DisplayName("Cannot target a non-Goblin creature")
    void cannotTargetNonGoblin() {
        Permanent oaf = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        oaf.setSummoningSick(false);

        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new LeafGilder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's Goblin")
    void cannotTargetOpponentGoblin() {
        Permanent oaf = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        oaf.setSummoningSick(false);

        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new BoggartForager());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not sacrifice a Goblin stolen after the ability resolves")
    void stolenGoblinIsNotSacrificed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent oaf = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        oaf.setSummoningSick(false);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartForager());

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, java.util.List.of(new SowerOfTemptation()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castCreature(player2, 0, goblin.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Boggart Forager");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boggart Forager");
        harness.assertNotInGraveyard(player1, "Boggart Forager");
    }

    @Test
    @DisplayName("An end-step activation expires at cleanup but sacrifices at the following end step")
    void endStepActivationWaitsForNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        Permanent oaf = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        oaf.setSummoningSick(false);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartForager());
        int basePower = gqs.getEffectivePower(gd, goblin);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower + 1);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertOnBattlefield(player1, "Boggart Forager");
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.FLYING)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Boggart Forager");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Boggart Forager");
        harness.assertInGraveyard(player1, "Boggart Forager");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick or already tapped")
    void tapCostRequiresReadyOaf() {
        Permanent oaf = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartForager());
        oaf.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        oaf.setSummoningSick(false);
        oaf.setTapped(true);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability has no effect when its Goblin is sacrificed in response")
    void targetLeavingBeforeResolutionStopsAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent oaf = harness.addToBattlefieldAndReturn(player1, new LowlandOaf());
        oaf.setSummoningSick(false);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartForager());
        Permanent otherGoblin = harness.addToBattlefieldAndReturn(player1, new BoggartForager());
        int basePower = gqs.getEffectivePower(gd, otherGoblin);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, goblin.getId())).isNull();
        assertThat(gqs.getEffectivePower(gd, otherGoblin)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, otherGoblin, Keyword.FLYING)).isFalse();
        assertThat(oaf.isTapped()).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.findPermanentById(gd, otherGoblin.getId())).isNotNull();
    }
}
