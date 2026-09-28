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
}
