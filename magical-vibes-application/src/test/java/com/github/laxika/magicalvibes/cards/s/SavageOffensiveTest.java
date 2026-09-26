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
        harness.addToBattlefield(player1, new HoodedKavu());
        harness.addToBattlefield(player2, new HoodedKavu());
        harness.castFromHand(player1, new SavageOffensive(), "{1}{R}");
        harness.passBothPriorities();

        Permanent ownKavu = findPermanent(player1, "Hooded Kavu");
        Permanent opponentKavu = findPermanent(player2, "Hooded Kavu");
        assertThat(gqs.hasKeyword(gd, ownKavu, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownKavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownKavu)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentKavu, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentKavu)).isEqualTo(2);
    }

    @Test
    void withKickerAlsoGivesOwnCreaturesPlusOnePlusOne() {
        harness.addToBattlefield(player1, new HoodedKavu());
        harness.addToBattlefield(player2, new HoodedKavu());
        harness.setHand(player1, List.of(new SavageOffensive()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        Permanent ownKavu = findPermanent(player1, "Hooded Kavu");
        Permanent opponentKavu = findPermanent(player2, "Hooded Kavu");
        assertThat(gqs.hasKeyword(gd, ownKavu, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownKavu)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentKavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentKavu)).isEqualTo(2);
    }

    @Test
    void effectsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new HoodedKavu());
        harness.setHand(player1, List.of(new SavageOffensive()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Hooded Kavu");
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, kavu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kavu)).isEqualTo(2);
    }
}
