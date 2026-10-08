package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AggravatedAssault;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchstalkerFrenzy.class, Forest.class, GrizzlyBears.class, HillGiant.class, AggravatedAssault.class})
class WitchstalkerFrenzyTest extends BaseCardTest {

    @Test
    void costsOneLessForEachCreatureThatAttackedThisTurn() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        declareAttackers(List.of(0));

        harness.setHand(player1, List.of(new WitchstalkerFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void countsCreaturesThatAttackedForAnyPlayer() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));

        harness.setHand(player1, List.of(new WitchstalkerFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WitchstalkerFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysFullCostWhenNoCreatureAttacked() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new WitchstalkerFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void fourAttackersReduceOnlyTheGenericCost() {
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
        Permanent target = addCreatureReady(player1, new HillGiant());
        declareAttackers(List.of(0, 1, 2, 3));
        harness.setHand(player1, List.of(new WitchstalkerFrenzy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void sameCreatureAttackingInTwoCombatsCountsOnlyOnce() {
        harness.addToBattlefield(player1, new AggravatedAssault());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> declareAttackers(List.of(1)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
        });
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);
        declareAttackers(List.of(1));
        harness.setHand(player1, List.of(new WitchstalkerFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void dealsExactlyFiveDamageToASurvivingCreature() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new WitchstalkerFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void stillCountsAnAttackerAfterItDies() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        declareAttackers(List.of(0));
        harness.setHand(player1, List.of(new WitchstalkerFrenzy(), new WitchstalkerFrenzy()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, attacker.getId());
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }
}
