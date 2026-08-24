package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AshenReaper;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HiddenPredators;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfAzgol.class, AshenReaper.class, GrizzlyBears.class, Disenchant.class,
        HiddenPredators.class})
class InvasionOfAzgolTest extends BaseCardTest {

    @Test
    void entersAndTargetPlayerSacrificesCreatureAndLosesLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvasionOfAzgol()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void transformedAshenReaperDoesNotGetCounterWithoutGraveyardPermanent() {
        Permanent battle = addBattleWithNoDefenseCounters();

        defeatBattle(battle);
        Permanent reaper = findPermanent(player1, "Ashen Reaper");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(reaper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void transformedAshenReaperGetsCounterWhenNoncreaturePermanentEntersGraveyard() {
        Permanent battle = addBattleWithNoDefenseCounters();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HiddenPredators());

        defeatBattle(battle);
        Permanent reaper = findPermanent(player1, "Ashen Reaper");

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Hidden Predators");
        harness.assertInGraveyard(player2, "Hidden Predators");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(reaper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addBattleWithNoDefenseCounters() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfAzgol());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        return battle;
    }

    private void defeatBattle(Permanent battle) {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
