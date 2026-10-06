package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.q.QalSismaBehemoth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SightOfTheScalelords.class, QalSismaBehemoth.class, DragonScarredBear.class,
        ColossodonYearling.class, Opalescence.class})
class SightOfTheScalelordsTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);
    }

    @Test
    @DisplayName("Beginning of combat boosts and grants vigilance to qualifying creatures you control")
    void boostsCreaturesWithToughnessAtLeastFour() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new QalSismaBehemoth());
        Permanent ineligible = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new QalSismaBehemoth());
        harness.addToBattlefield(player1, new SightOfTheScalelords());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, eligible)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, eligible)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, eligible, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ineligible)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ineligible)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ineligible, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new QalSismaBehemoth());
        harness.addToBattlefield(player1, new SightOfTheScalelords());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, eligible)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, eligible, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, eligible)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, eligible, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A creature with exactly four toughness gets both benefits")
    void includesExactlyFourToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.addToBattlefield(player1, new SightOfTheScalelords());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Sight does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        harness.addToBattlefield(player1, new SightOfTheScalelords());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Qualifying creatures entering before resolution receive both benefits")
    void evaluatesCreaturesWhenTriggerResolves() {
        harness.addToBattlefield(player1, new SightOfTheScalelords());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the benefits")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        harness.addToBattlefield(player1, new SightOfTheScalelords());
        advanceToCombatAndResolve(player1);

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Sight grants itself vigilance when Opalescence makes it a qualifying creature")
    void animatedSightReceivesBothBenefits() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent sight = harness.addToBattlefieldAndReturn(player1, new SightOfTheScalelords());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, sight)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sight)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, sight, Keyword.VIGILANCE)).isTrue();
    }
}
