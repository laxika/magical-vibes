package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({WardenOfTheFirstTree.class, WingsOfVelisVel.class})
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

    @Test
    @DisplayName("Returning to Human Warrior removes Spirit but retains granted keywords and counters")
    void firstAbilityRemovesSpiritWithoutRemovingKeywordsOrCounters() {
        Permanent warden = addWarden();
        activate(warden, 0, 2);
        resetPriority();
        activate(warden, 1, 4);
        resetPriority();
        activate(warden, 2, 6);
        resetPriority();
        activate(warden, 0, 2);

        assertThat(gqs.effectiveCreatureSubtypes(gd, warden))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.WARRIOR);
        assertThat(gqs.hasKeyword(gd, warden, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(8);

        resetPriority();
        activate(warden, 2, 6);
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(8);
    }

    @Test
    @DisplayName("Second ability leaves a different base power and toughness unchanged")
    void secondAbilityDoesNotSetBasePowerAndToughness() {
        Permanent warden = addWarden();
        activate(warden, 0, 2);
        resetPriority();
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, warden.getId());
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);

        resetPriority();
        activate(warden, 1, 4);

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, warden, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Repeated third activations accumulate counters and hybrid costs accept black mana")
    void repeatedThirdAbilityAddsCountersWithBlackMana() {
        Permanent warden = addWarden();
        harness.addMana(player1, ManaColor.BLACK, 18);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(warden);
        harness.activateAbility(player1, permanentIndex, 0, null, null);
        harness.passBothPriorities();
        resetPriority();
        harness.activateAbility(player1, permanentIndex, 1, null, null);
        harness.passBothPriorities();
        resetPriority();
        harness.activateAbility(player1, permanentIndex, 2, null, null);
        harness.passBothPriorities();
        resetPriority();
        harness.activateAbility(player1, permanentIndex, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(13);
    }
}
