package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({ToweringThunderfist.class})
class ToweringThunderfistTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability grants vigilance until end of turn")
    void resolvingGrantsVigilance() {
        Permanent thunderfist = addCreatureReady(player1, new ToweringThunderfist());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(gqs.hasKeyword(gd, thunderfist, Keyword.VIGILANCE)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, thunderfist, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Granted vigilance wears off at end of turn")
    void vigilanceWearsOff() {
        Permanent thunderfist = addCreatureReady(player1, new ToweringThunderfist());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thunderfist, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Activating the ability does not tap Towering Thunderfist")
    void activatingDoesNotTap() {
        Permanent thunderfist = addCreatureReady(player1, new ToweringThunderfist());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(thunderfist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without white mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new ToweringThunderfist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Vigilance lets Towering Thunderfist attack without tapping")
    void attacksWithoutTapping() {
        Permanent thunderfist = addCreatureReady(player1, new ToweringThunderfist());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(thunderfist.isAttacking()).isTrue();
        assertThat(thunderfist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Towering Thunderfist can activate without untapping")
    void activatesWhileTappedAndSummoningSick() {
        Permanent thunderfist = harness.addToBattlefieldAndReturn(player1, new ToweringThunderfist());
        thunderfist.setSummoningSick(true);
        thunderfist.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thunderfist, Keyword.VIGILANCE)).isTrue();
        assertThat(thunderfist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The vigilance ability affects only its source")
    void grantsVigilanceOnlyToSource() {
        Permanent source = addCreatureReady(player1, new ToweringThunderfist());
        Permanent other = addCreatureReady(player1, new ToweringThunderfist());
        Permanent opponent = addCreatureReady(player2, new ToweringThunderfist());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }
}
