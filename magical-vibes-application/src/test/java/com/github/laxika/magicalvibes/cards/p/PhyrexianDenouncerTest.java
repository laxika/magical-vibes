package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.p.PlagueBeetle;
import com.github.laxika.magicalvibes.cards.p.PhyrexianPlaguelord;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianDenouncer.class, PhyrexianPlaguelord.class, PlagueBeetle.class, TreetopVillage.class})
class PhyrexianDenouncerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap and sacrifice ability gives a target creature -1/-1")
    void abilityGivesTargetCreatureMinusOneMinusOne() {
        addCreatureReady(player1, new PhyrexianDenouncer());
        Permanent target = addCreatureReady(player2, new PhyrexianPlaguelord());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Denouncer");
        harness.assertInGraveyard(player1, "Phyrexian Denouncer");
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PhyrexianDenouncer());
        Permanent target = addCreatureReady(player2, new PhyrexianPlaguelord());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new PhyrexianDenouncer());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TreetopVillage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot be activated with summoning sickness")
    void abilityCannotBeActivatedWithSummoningSickness() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PhyrexianDenouncer());
        Permanent target = addCreatureReady(player2, new PlagueBeetle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated while the source is tapped")
    void abilityCannotBeActivatedWhileTapped() {
        Permanent source = addCreatureReady(player1, new PhyrexianDenouncer());
        source.tap();
        Permanent target = addCreatureReady(player2, new PlagueBeetle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The -1/-1 effect puts a creature with one toughness into its graveyard")
    void abilityKillsOneToughnessCreature() {
        addCreatureReady(player1, new PhyrexianDenouncer());
        Permanent target = addCreatureReady(player2, new PlagueBeetle());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Plague Beetle");
        harness.assertInGraveyard(player2, "Plague Beetle");
    }
}
