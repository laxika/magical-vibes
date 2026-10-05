package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyoshiWarriorExemplars.class, Forest.class, GrizzlyBears.class})
class KyoshiWarriorExemplarsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with eight lands boosts your creatures by +2/+2")
    void attacksWithEightLandsBoostsYourCreatures() {
        Permanent kyoshi = addCreatureReady(player1, new KyoshiWarriorExemplars());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        addForests(player1, 8);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kyoshi)));
        resolveAllTriggers();

        assertThat(kyoshi.getPowerModifier()).isEqualTo(2);
        assertThat(kyoshi.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(opponent.getPowerModifier()).isEqualTo(0);
        assertThat(opponent.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not boost when you control fewer than eight lands")
    void doesNotBoostWithFewerThanEightLands() {
        Permanent kyoshi = addCreatureReady(player1, new KyoshiWarriorExemplars());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addForests(player1, 7);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kyoshi)));
        resolveAllTriggers();

        assertThat(kyoshi.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent kyoshi = addCreatureReady(player1, new KyoshiWarriorExemplars());
        addForests(player1, 8);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kyoshi)));
        resolveAllTriggers();

        assertThat(kyoshi.getPowerModifier()).isEqualTo(2);
        assertThat(kyoshi.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kyoshi.getPowerModifier()).isEqualTo(0);
        assertThat(kyoshi.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Losing the eighth land before resolution prevents the boost")
    void losingEighthLandBeforeResolutionPreventsBoost() {
        Permanent kyoshi = addCreatureReady(player1, new KyoshiWarriorExemplars());
        addForests(player1, 7);
        Permanent eighthLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(eighthLand);
        gd.playerGraveyards.get(player1.getId()).add(eighthLand.getCard());
        resolveAllTriggers();

        assertThat(kyoshi.getPowerModifier()).isZero();
        assertThat(kyoshi.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Gaining the eighth land after attacking does not create a trigger")
    void gainingEighthLandAfterAttackingDoesNotTrigger() {
        Permanent kyoshi = addCreatureReady(player1, new KyoshiWarriorExemplars());
        addForests(player1, 7);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new Forest());
        resolveAllTriggers();

        assertThat(kyoshi.getPowerModifier()).isZero();
        assertThat(kyoshi.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Opponent lands do not count toward the eight-land threshold")
    void opponentLandsDoNotCount() {
        Permanent kyoshi = addCreatureReady(player1, new KyoshiWarriorExemplars());
        addForests(player1, 7);
        addForests(player2, 8);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(kyoshi.getPowerModifier()).isZero();
        assertThat(kyoshi.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Only creatures present when the trigger resolves receive the boost")
    void boostUsesCreaturesPresentAtResolution() {
        Permanent kyoshi = addCreatureReady(player1, new KyoshiWarriorExemplars());
        addForests(player1, 9);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriorExemplars());
        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriorExemplars());

        assertThat(kyoshi.getPowerModifier()).isEqualTo(2);
        assertThat(kyoshi.getToughnessModifier()).isEqualTo(2);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(2);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(2);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    private void addForests(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
