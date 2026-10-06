package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SephirothPlanetsHeir.class, HillGiant.class, GrizzlyBears.class, CruelEdict.class})
class SephirothPlanetsHeirTest extends BaseCardTest {

    @Test
    @DisplayName("The enters-the-battlefield ability shrinks only opponents' creatures until end of turn")
    void etbShrinksOpponentsCreaturesOnlyUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castSephiroth();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's creature dying puts a +1/+1 counter on Sephiroth")
    void gainsCounterWhenOpponentCreatureDies() {
        Permanent sephiroth = harness.addToBattlefieldAndReturn(player1, new SephirothPlanetsHeir());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(sephiroth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each opponent creature killed by the enters ability gives Sephiroth a counter")
    void gainsOneCounterForEachCreatureKilledByEtb() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSephiroth();

        Permanent sephiroth = findPermanent(player1, "Sephiroth, Planet's Heir");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(sephiroth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after the enters ability resolves are not shrunk")
    void laterCreaturesAreNotAffected() {
        castSephiroth();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
        assertThat(findPermanent(player1, "Sephiroth, Planet's Heir")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Deaths of its controller's creatures do not give Sephiroth counters")
    void ownCreatureDeathsDoNotGiveCounters() {
        Permanent originalSephiroth = harness.addToBattlefieldAndReturn(player2, new SephirothPlanetsHeir());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSephiroth();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(originalSephiroth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Sephiroth, Planet's Heir")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castSephiroth() {
        harness.setHand(player1, List.of(new SephirothPlanetsHeir()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
