package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BairdArgivianRecruiter.class, GrizzlyBears.class, QueenAllenalOfRuadach.class})
class BairdArgivianRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier token when you control a creature with greater power than base")
    void createsSoldierWhenYouControlModifiedCreature() {
        addBaird();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setPowerModifier(1);

        resolveEndStep();

        Permanent token = findToken();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Does not create a token when your creatures have only base power")
    void doesNotCreateSoldierWhenNoModifiedCreature() {
        addBaird();
        addCreatureReady(player1, new GrizzlyBears());

        resolveEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("An opponent's modified creature does not satisfy the condition")
    void opponentModifiedCreatureDoesNotSatisfyCondition() {
        addBaird();
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        opponentBear.setPowerModifier(1);

        resolveEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void bairdCanSatisfyHisOwnCondition() {
        Permanent baird = harness.addToBattlefieldAndReturn(player1, new BairdArgivianRecruiter());
        baird.setPowerModifier(1);

        resolveEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void conditionMustStillBeTrueWhenAbilityResolves() {
        Permanent baird = harness.addToBattlefieldAndReturn(player1, new BairdArgivianRecruiter());
        baird.setPowerModifier(1);
        beginEndStep();
        assertThat(gd.stack).hasSize(1);

        baird.setPowerModifier(0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void doesNotTriggerWhenConditionIsFalseAtBeginningOfEndStep() {
        addBaird();

        beginEndStep();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent baird = harness.addToBattlefieldAndReturn(player1, new BairdArgivianRecruiter());
        baird.setPowerModifier(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void characteristicDefiningPowerIsBasePower() {
        addBaird();
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());

        beginEndStep();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleQualifyingCreaturesStillCreateOnlyOneToken() {
        Permanent baird = harness.addToBattlefieldAndReturn(player1, new BairdArgivianRecruiter());
        baird.setPowerModifier(1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setPowerModifier(1);

        resolveEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    private void addBaird() {
        harness.addToBattlefield(player1, new BairdArgivianRecruiter());
    }

    private Permanent findToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void resolveEndStep() {
        beginEndStep();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
    }
}
