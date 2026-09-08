package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FightAsOne.class, GrizzlyBears.class, YouthfulKnight.class})
class FightAsOneTest extends BaseCardTest {

    @Test
    void humanModeBoostsAndMakesHumanCreatureIndestructible() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());

        castFightAsOne(new int[]{0}, List.of(human.getId()));

        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void nonHumanModeBoostsAndMakesNonHumanCreatureIndestructible() {
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFightAsOne(new int[]{1}, List.of(nonHuman.getId()));

        assertThat(nonHuman.getPowerModifier()).isEqualTo(1);
        assertThat(nonHuman.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void bothModesResolveForTheirSeparateCreatureTypes() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFightAsOne(new int[]{0, 1}, List.of(human.getId(), nonHuman.getId()));

        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(nonHuman.getPowerModifier()).isEqualTo(1);
        assertThat(nonHuman.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void cannotTargetCreatureYouDoNotControl() {
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());

        assertThatThrownBy(() -> castFightAsOne(new int[]{0}, List.of(opponentHuman.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFightAsOne(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new FightAsOne()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modeIndices, targetIds);
        harness.passBothPriorities();
    }
}
