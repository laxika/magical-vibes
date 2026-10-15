package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.c.CheckpointOfficer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FightAsOne.class, AlmightyBrushwagg.class, CheckpointOfficer.class})
class FightAsOneTest extends BaseCardTest {

    @Test
    void humanModeBoostsAndMakesHumanCreatureIndestructible() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new CheckpointOfficer());

        castFightAsOne(new int[]{0}, List.of(human.getId()));

        assertThat(human.getPowerModifier()).isEqualTo(1);
        assertThat(human.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void nonHumanModeBoostsAndMakesNonHumanCreatureIndestructible() {
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());

        castFightAsOne(new int[]{1}, List.of(nonHuman.getId()));

        assertThat(nonHuman.getPowerModifier()).isEqualTo(1);
        assertThat(nonHuman.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void bothModesResolveForTheirSeparateCreatureTypes() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new CheckpointOfficer());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());

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
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new CheckpointOfficer());

        assertThatThrownBy(() -> castFightAsOne(new int[]{0}, List.of(opponentHuman.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void humanModeRejectsNonHumanCreature() {
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());

        assertThatThrownBy(() -> castFightAsOne(new int[]{0}, List.of(nonHuman.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonHumanModeRejectsHumanCreature() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new CheckpointOfficer());

        assertThatThrownBy(() -> castFightAsOne(new int[]{1}, List.of(human.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonHumanModeRejectsOpponentCreature() {
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());

        assertThatThrownBy(() -> castFightAsOne(new int[]{1}, List.of(nonHuman.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesStillAffectNonHumanWhenHumanTargetLeaves() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new CheckpointOfficer());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new FightAsOne()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(human.getId(), nonHuman.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(human);
        gd.playerHands.get(player1.getId()).add(human.getCard());
        harness.passBothPriorities();

        assertThat(nonHuman.getPowerModifier()).isEqualTo(1);
        assertThat(nonHuman.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(human.getPowerModifier()).isZero();
        assertThat(human.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void bothModesExpireAtEndOfTurn() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new CheckpointOfficer());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new AlmightyBrushwagg());
        castFightAsOne(new int[]{0, 1}, List.of(human.getId(), nonHuman.getId()));

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(human.getPowerModifier()).isZero();
        assertThat(human.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(nonHuman.getPowerModifier()).isZero();
        assertThat(nonHuman.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isFalse();
    }
    private void castFightAsOne(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new FightAsOne()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modeIndices, targetIds);
        harness.passBothPriorities();
    }
}
