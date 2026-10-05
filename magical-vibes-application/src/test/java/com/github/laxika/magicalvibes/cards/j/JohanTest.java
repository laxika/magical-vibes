package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Johan.class, KoboldsOfKherKeep.class, Humble.class})
class JohanTest extends BaseCardTest {

    private void resolveCombatMay(boolean accepted) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
    }

    @Test
    void acceptingMayLetsOtherCreaturesAttackWithoutTapping() {
        Permanent johan = addCreatureReady(player1, new Johan());
        Permanent kobold = addCreatureReady(player1, new KoboldsOfKherKeep());

        resolveCombatMay(true);
        declareAttackers(List.of(1));

        assertThat(johan.isTapped()).isFalse();
        assertThat(kobold.isTapped()).isFalse();
    }

    @Test
    void acceptingMayPreventsJohanFromAttacking() {
        Permanent johan = addCreatureReady(player1, new Johan());
        addCreatureReady(player1, new KoboldsOfKherKeep());

        resolveCombatMay(true);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(johan.isAttacking()).isFalse();
    }

    @Test
    void decliningMayLetsJohanAttackAndTap() {
        Permanent johan = addCreatureReady(player1, new Johan());

        resolveCombatMay(false);
        declareAttackers(List.of(0));

        assertThat(johan.isTapped()).isTrue();
    }

    @Test
    void tappedJohanDoesNotPreventAttackersFromTapping() {
        Permanent johan = addCreatureReady(player1, new Johan());
        Permanent kobold = addCreatureReady(player1, new KoboldsOfKherKeep());

        resolveCombatMay(true);
        johan.tap();
        declareAttackers(List.of(1));

        assertThat(kobold.isTapped()).isTrue();
    }

    @Test
    void combatPermissionExpiresAtEndOfCombat() {
        addCreatureReady(player1, new Johan());
        Permanent kobold = addCreatureReady(player1, new KoboldsOfKherKeep());

        resolveCombatMay(true);
        declareAttackers(List.of(1));
        assertThat(kobold.isTapped()).isFalse();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        declareAttackers(List.of(1));
        assertThat(kobold.isTapped()).isTrue();
    }

    @Test
    void untappingJohanBeforeAttacksEnablesPermissionEvenIfHeWasTappedOnResolution() {
        Permanent johan = addCreatureReady(player1, new Johan());
        Permanent kobold = addCreatureReady(player1, new KoboldsOfKherKeep());
        johan.tap();

        resolveCombatMay(true);
        johan.untap();
        declareAttackers(List.of(1));

        assertThat(kobold.isTapped()).isFalse();
    }

    @Test
    void opponentJohanDoesNotTriggerOnYourTurn() {
        addCreatureReady(player2, new Johan());
        Permanent kobold = addCreatureReady(player1, new KoboldsOfKherKeep());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        declareAttackers(List.of(0));

        assertThat(kobold.isTapped()).isTrue();
    }

    @Test
    void johanCanAttackInLaterCombatAfterRestrictionExpires() {
        Permanent johan = addCreatureReady(player1, new Johan());
        resolveCombatMay(true);
        declareAttackers(List.of());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        declareAttackers(List.of(0));

        assertThat(johan.isTapped()).isTrue();
    }

    @Test
    void removingGrantedCantAttackAbilityAllowsJohanToAttackWithoutTapping() {
        Permanent johan = addCreatureReady(player1, new Johan());
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            resolveCombatMay(true);
            harness.setHand(player1, List.of(new Humble()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castInstant(player1, 0, johan.getId());
            harness.passBothPriorities();
        });

        declareAttackers(List.of(0));

        assertThat(johan.isTapped()).isFalse();
    }
}
