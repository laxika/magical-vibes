package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HoodedKavu;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SavageOffensive.class, HoodedKavu.class})
class SavageOffensiveTest extends BaseCardTest {

    @Test
    void withoutKickerGrantsFirstStrikeButNotThePump() {
        Permanent ownKavu = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());
        Permanent opponentKavu = harness.addToBattlefieldAndReturn(player2, new HoodedKavu());
        harness.castFromHand(player1, new SavageOffensive(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownKavu, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownKavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownKavu)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentKavu, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentKavu)).isEqualTo(2);
    }

    @Test
    void withKickerAlsoGivesOwnCreaturesPlusOnePlusOne() {
        Permanent ownKavu = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());
        Permanent opponentKavu = harness.addToBattlefieldAndReturn(player2, new HoodedKavu());
        harness.setHand(player1, List.of(new SavageOffensive()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownKavu, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownKavu)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentKavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentKavu)).isEqualTo(2);
    }

    @Test
    void effectsWearOffAtCleanup() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());
        harness.setHand(player1, List.of(new SavageOffensive()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveEitherEffect() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());
        harness.setHand(player1, List.of(new SavageOffensive()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new HoodedKavu());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);
    }

    @Test
    void creaturesEnteringBeforeResolutionReceiveBothEffects() {
        harness.setHand(player1, List.of(new SavageOffensive()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castKickedSorcery(player1, 0);

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new HoodedKavu());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(3);
    }
}
