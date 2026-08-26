package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrimordialPlasm;
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

@CardUsed({GrizzlyBears.class, InvasionOfMuraganda.class, PrimordialPlasm.class})
class InvasionOfMuragandaTest extends BaseCardTest {

    @Test
    void entersPutsCounterOnFriendlyCreatureAndFightsOptionalOpponentCreature() {
        Permanent friendly = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        castInvasion(List.of(friendly.getId(), opponent.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(friendly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(friendly.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponent);
    }

    @Test
    void mayChooseNoOpponentCreatureForTheFight() {
        Permanent friendly = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        castInvasion(List.of(friendly.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(friendly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(friendly.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
    }

    @Test
    void defeatCastsPrimordialPlasmTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfMuraganda());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent plasm = findPermanent(player1, "Primordial Plasm");
        assertThat(plasm.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(battle);
    }

    @Test
    void primordialPlasmBoostsAndRemovesAbilitiesFromAnotherCreatureAtCombat() {
        Permanent plasm = addCreatureReady(player1, new PrimordialPlasm());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.isLosesAllAbilitiesUntilEndOfTurn()).isTrue();
        assertThat(plasm.isLosesAllAbilitiesUntilEndOfTurn()).isFalse();
    }

    private void castInvasion(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new InvasionOfMuraganda()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, targetIds);
    }
}
