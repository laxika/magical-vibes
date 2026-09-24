package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunstrikeLegionnaire.class, BenalishKnight.class, GrizzlyBears.class, HillGiant.class, Spellbook.class})
class SunstrikeLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent legionnaire = addReadyLegionnaire(player1);
        legionnaire.tap();

        advanceToUpkeep(player1);

        assertThat(legionnaire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps when another creature enters under its controller's control")
    void untapsWhenAnotherCreatureEntersUnderControllerControl() {
        Permanent legionnaire = addReadyLegionnaire(player1);
        legionnaire.tap();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(legionnaire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps when another creature enters under an opponent's control")
    void untapsWhenAnotherCreatureEntersUnderOpponentsControl() {
        Permanent legionnaire = addReadyLegionnaire(player1);
        legionnaire.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(legionnaire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when Sunstrike Legionnaire itself enters")
    void doesNotTriggerForSelfEntering() {
        harness.castFromHand(player1, new SunstrikeLegionnaire(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent enters")
    void doesNotTriggerForNoncreaturePermanent() {
        Permanent legionnaire = addReadyLegionnaire(player1);
        legionnaire.tap();

        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(legionnaire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps a target creature with mana value 3 or less")
    void tapsTargetCreatureWithManaValueThreeOrLess() {
        addReadyLegionnaire(player1);
        Permanent target = addCreatureReady(player2, new BenalishKnight());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 3")
    void cannotTargetCreatureWithManaValueGreaterThanThree() {
        addReadyLegionnaire(player1);
        Permanent target = addCreatureReady(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");
    }

    @Test
    @DisplayName("Can tap a creature its controller controls")
    void canTapOwnCreature() {
        addReadyLegionnaire(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the tap ability while the Legionnaire is tapped")
    void cannotActivateWhileTapped() {
        Permanent legionnaire = addReadyLegionnaire(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(legionnaire.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyLegionnaire(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with mana value 3 or less");
    }

    private Permanent addReadyLegionnaire(Player player) {
        return addCreatureReady(player, new SunstrikeLegionnaire());
    }
}
