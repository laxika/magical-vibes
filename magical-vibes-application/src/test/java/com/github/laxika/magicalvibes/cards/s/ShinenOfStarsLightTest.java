package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KitsuneBonesetter;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShinenOfStarsLight.class, KitsuneBonesetter.class, MirenTheMoaningWell.class})
class ShinenOfStarsLightTest extends BaseCardTest {

    @Test
    @DisplayName("Channel gives target creature first strike until end of turn")
    void channelGrantsFirstStrike() {
        harness.setHand(player1, List.of(new ShinenOfStarsLight()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KitsuneBonesetter());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Shinen of Stars' Light");
    }

    @Test
    @DisplayName("Channel can target a creature an opponent controls")
    void channelCanTargetOpposingCreature() {
        harness.setHand(player1, List.of(new ShinenOfStarsLight()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KitsuneBonesetter());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Channel's first strike wears off at end of turn")
    void channelFirstStrikeWearsOff() {
        harness.setHand(player1, List.of(new ShinenOfStarsLight()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KitsuneBonesetter());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Channel cannot target a noncreature permanent")
    void channelRejectsNoncreatureTarget() {
        harness.setHand(player1, List.of(new ShinenOfStarsLight()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Shinen of Stars' Light");
        assertThat(gd.stack).isEmpty();
    }
}
