package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.u.UnexplainedDisappearance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenMesmerist.class, VernadiShieldmate.class,
        UnexplainedDisappearance.class})
class VedalkenMesmeristTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking lets you give an opponent's creature -2/-0 until end of turn")
    void debuffsTargetOpponentCreatureOnAttack() {
        addCreatureReady(player1, new VedalkenMesmerist());
        Permanent shieldmate = addCreatureReady(player2, new VernadiShieldmate());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, shieldmate.getId());
        resolveAllTriggers();

        assertThat(shieldmate.getPowerModifier()).isEqualTo(-2);
        assertThat(shieldmate.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The -2/-0 debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_STEP));
        addCreatureReady(player1, new VedalkenMesmerist());
        Permanent shieldmate = addCreatureReady(player2, new VernadiShieldmate());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, shieldmate.getId());
        resolveAllTriggers();

        gs.declareBlockers(gd, player2, List.of());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shieldmate.getPowerModifier()).isZero();
        assertThat(shieldmate.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addCreatureReady(player1, new VedalkenMesmerist());
        Permanent ownShieldmate = addCreatureReady(player1, new VernadiShieldmate());
        Permanent opponentShieldmate = addCreatureReady(player2, new VernadiShieldmate());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownShieldmate.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentShieldmate.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The attack trigger is skipped when no opponent creature is available")
    void skippedWhenNoOpponentCreature() {
        addCreatureReady(player1, new VedalkenMesmerist());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger resolves even when Mesmerist leaves the battlefield")
    void triggerResolvesAfterSourceIsReturnedToHand() {
        Permanent mesmerist = addCreatureReady(player1, new VedalkenMesmerist());
        Permanent target = addCreatureReady(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, mesmerist.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Vedalken Mesmerist");
        harness.assertInHand(player1, "Vedalken Mesmerist");
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Returning the target to hand prevents the attack trigger from affecting it")
    void triggerDoesNotAffectTargetThatLeavesBattlefield() {
        addCreatureReady(player1, new VedalkenMesmerist());
        Permanent target = addCreatureReady(player2, new VernadiShieldmate());
        Permanent otherCreature = addCreatureReady(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInHand(player2, "Vernadi Shieldmate");
        assertThat(otherCreature.getPowerModifier()).isZero();
        assertThat(otherCreature.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
