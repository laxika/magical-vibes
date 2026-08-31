package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WardenOfTheFirstTreeTest extends BaseCardTest {

    private Permanent addWarden() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new WardenOfTheFirstTree());
    }

    private void resetPriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activate(Permanent warden, int abilityIndex, int mana) {
        harness.addMana(player1, ManaColor.WHITE, mana);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(warden);
        harness.activateAbility(player1, permanentIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("First ability makes the Warden a 3/3 Human Warrior")
    void firstAbilityMakesHumanWarrior() {
        Permanent warden = addWarden();

        activate(warden, 0, 2);

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, warden))
                .contains(CardSubtype.HUMAN, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Second ability requires Warrior and grants trample and lifelink indefinitely")
    void secondAbilityRequiresWarriorAndGrantsKeywords() {
        Permanent warden = addWarden();

        activate(warden, 0, 2);
        resetPriority();
        activate(warden, 1, 4);

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, warden))
                .contains(CardSubtype.HUMAN, CardSubtype.SPIRIT, CardSubtype.WARRIOR);
        assertThat(gqs.hasKeyword(gd, warden, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Second ability does not grant keywords without Warrior")
    void secondAbilityRequiresWarrior() {
        Permanent warden = addWarden();

        activate(warden, 1, 4);

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, warden, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Third ability requires Spirit and puts five +1/+1 counters on the Warden")
    void thirdAbilityRequiresSpiritAndAddsCounters() {
        Permanent warden = addWarden();

        activate(warden, 2, 6);
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(1);

        resetPriority();
        activate(warden, 0, 2);
        resetPriority();
        activate(warden, 1, 4);
        resetPriority();
        activate(warden, 2, 6);

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(8);
    }
}
