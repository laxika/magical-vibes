package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GnarlidPack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TideforceElemental.class, TectonicEdge.class, GnarlidPack.class})
class TideforceElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers to untap Tideforce Elemental")
    void landfallUntapsWhenAccepted() {
        Permanent elemental = addReadyElemental();
        elemental.tap();
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining landfall does not untap Tideforce Elemental")
    void landfallDoesNotUntapWhenDeclined() {
        Permanent elemental = addReadyElemental();
        elemental.tap();
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(elemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent elemental = addReadyElemental();
        elemental.tap();
        harness.setHand(player2, List.of(new TectonicEdge()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player2, 0);

        assertThat(elemental.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability taps an untapped target creature")
    void abilityTapsUntappedCreature() {
        addReadyElemental();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability untaps a tapped target creature")
    void abilityUntapsTappedCreature() {
        addReadyElemental();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target Tideforce Elemental itself")
    void abilityCannotTargetItself() {
        Permanent elemental = addReadyElemental();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elemental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("The controller may decline to tap an untapped target")
    void mayDeclineToTapTarget() {
        Permanent elemental = addReadyElemental();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlidPack());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(elemental.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(target.isTapped()).isFalse();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(target.isTapped()).isFalse();
        assertThat(elemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller may decline to untap a tapped target")
    void mayDeclineToUntapTarget() {
        addReadyElemental();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GnarlidPack());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(target.isTapped()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature land")
    void abilityCannotTargetNoncreature() {
        addReadyElemental();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyElemental() {
        return addCreatureReady(player1, new TideforceElemental());
    }
}
