package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.g.GangOfElk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianDefiler.class, GangOfElk.class, Crawlspace.class, PhyrexianBroodlings.class})
class PhyrexianDefilerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap and sacrifice ability gives a target creature -3/-3")
    void abilityGivesTargetCreatureMinusThreeMinusThree() {
        addCreatureReady(player1, new PhyrexianDefiler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GangOfElk());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Defiler");
        harness.assertInGraveyard(player1, "Phyrexian Defiler");
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -3/-3 effect wears off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PhyrexianDefiler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GangOfElk());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new PhyrexianDefiler());
        Permanent crawlspace = harness.addToBattlefieldAndReturn(player2, new Crawlspace());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, crawlspace.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot be activated with summoning sickness")
    void abilityCannotBeActivatedWithSummoningSickness() {
        harness.addToBattlefield(player1, new PhyrexianDefiler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GangOfElk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("The ability cannot be activated when the source is already tapped")
    void abilityCannotBeActivatedWhenAlreadyTapped() {
        Permanent source = addCreatureReady(player1, new PhyrexianDefiler());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GangOfElk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The -3/-3 effect destroys a 2/2 creature")
    void abilityDestroysTwoTwoCreature() {
        addCreatureReady(player1, new PhyrexianDefiler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianBroodlings());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Broodlings");
        harness.assertInGraveyard(player2, "Phyrexian Broodlings");
    }
}
