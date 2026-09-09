package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunstrikeLegionnaire.class, GrayOgre.class, GrizzlyBears.class, HillGiant.class, Spellbook.class})
class SunstrikeLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent legionnaire = addReadyLegionnaire(player1);
        legionnaire.tap();

        advanceToNextTurn(player2);

        assertThat(legionnaire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps when another creature enters under its controller's control")
    void untapsWhenAnotherCreatureEntersUnderControllerControl() {
        Permanent legionnaire = addReadyLegionnaire(player1);
        legionnaire.tap();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(legionnaire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when Sunstrike Legionnaire itself enters")
    void doesNotTriggerForSelfEntering() {
        harness.setHand(player1, List.of(new SunstrikeLegionnaire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent enters")
    void doesNotTriggerForNoncreaturePermanent() {
        Permanent legionnaire = addReadyLegionnaire(player1);
        legionnaire.tap();

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(legionnaire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps a target creature with mana value 3 or less")
    void tapsTargetCreatureWithManaValueThreeOrLess() {
        addReadyLegionnaire(player1);
        Permanent target = addCreatureReady(player2, new GrayOgre());

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

    private Permanent addReadyLegionnaire(Player player) {
        return addCreatureReady(player, new SunstrikeLegionnaire());
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
