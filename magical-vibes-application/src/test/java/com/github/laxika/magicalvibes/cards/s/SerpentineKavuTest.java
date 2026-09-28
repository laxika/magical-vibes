package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SerpentineKavu.class)
class SerpentineKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R} grants haste until end of turn")
    void payingRedGrantsHaste() {
        Permanent kavu = addCreatureReady(player1, new SerpentineKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent kavu = addCreatureReady(player1, new SerpentineKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Requires red mana")
    void requiresRedMana() {
        addCreatureReady(player1, new SerpentineKavu());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Grants haste only to the source creature")
    void grantsHasteOnlyToSourceCreature() {
        Permanent kavu = addCreatureReady(player1, new SerpentineKavu());
        Permanent otherKavu = addCreatureReady(player1, new SerpentineKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherKavu, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can activate twice without tapping")
    void canActivateTwiceWithoutTapping() {
        Permanent kavu = addCreatureReady(player1, new SerpentineKavu());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kavu.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste lets a newly entered Kavu attack")
    void hasteLetsNewlyEnteredKavuAttack() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new SerpentineKavu());
        addCreatureReady(player2, new SerpentineKavu());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.HASTE)).isTrue();
        assertThat(als.canAttack(gd, kavu, player1.getId())).isTrue();
        declareAttackers(List.of(0));

        assertThat(kavu.isAttacking()).isTrue();
    }
}
