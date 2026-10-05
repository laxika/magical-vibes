package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimarisChaplain.class, GrizzlyBears.class})
class PrimarisChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("When Primaris Chaplain attacks, it gains indestructible and battle cry boosts other attackers")
    void attackTriggersRosariusAndBattleCry() {
        Permanent chaplain = addCreatureReady(player1, new PrimarisChaplain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(chaplain.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(chaplain.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Primaris Chaplain's attack indestructible and battle cry bonuses wear off at end of turn")
    void attackBonusesWearOffAtEndOfTurn() {
        Permanent chaplain = addCreatureReady(player1, new PrimarisChaplain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(chaplain.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(chaplain.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Primaris Chaplain's abilities do not trigger when it does not attack")
    void abilitiesDoNotTriggerWhenChaplainStaysBack() {
        Permanent chaplain = addCreatureReady(player1, new PrimarisChaplain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(chaplain.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking alone grants indestructible without boosting the Chaplain itself")
    void attackingAloneGrantsOnlyIndestructible() {
        Permanent chaplain = addCreatureReady(player1, new PrimarisChaplain());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(chaplain.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(chaplain.getPowerModifier()).isZero();
        assertThat(chaplain.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Battle cry does not boost creatures that stay back or grant them indestructible")
    void nonattackingChaplainGetsNeitherBonus() {
        Permanent attacker = addCreatureReady(player1, new PrimarisChaplain());
        Permanent stayingBack = addCreatureReady(player1, new PrimarisChaplain());
        Permanent opponent = addCreatureReady(player2, new PrimarisChaplain());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(stayingBack.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(stayingBack.getPowerModifier()).isZero();
        assertThat(opponent.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking Chaplain's battle cry boosts the other attackers independently")
    void multipleBattleCryTriggersAccumulate() {
        Permanent first = addCreatureReady(player1, new PrimarisChaplain());
        Permanent second = addCreatureReady(player1, new PrimarisChaplain());
        Permanent third = addCreatureReady(player1, new PrimarisChaplain());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        for (Permanent chaplain : List.of(first, second, third)) {
            assertThat(chaplain.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
            assertThat(chaplain.getPowerModifier()).isEqualTo(2);
            assertThat(chaplain.getToughnessModifier()).isZero();
        }
    }
}
