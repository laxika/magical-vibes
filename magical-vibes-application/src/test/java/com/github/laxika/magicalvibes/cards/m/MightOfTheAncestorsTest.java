package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SeekerOfSunlight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightOfTheAncestors.class, SeekerOfSunlight.class})
class MightOfTheAncestorsTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat gives a controlled creature +2/+0 and vigilance")
    void boostsAndGrantsVigilanceToTarget() {
        addMight();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SeekerOfSunlight());

        resolveBeginningOfCombat(player1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The trigger can target only a creature you control")
    void targetsOnlyCreaturesYouControl() {
        addMight();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SeekerOfSunlight());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SeekerOfSunlight());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId(), gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost and vigilance expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        addMight();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SeekerOfSunlight());

        resolveBeginningOfCombat(player1, target);
        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("No ability triggers during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        addMight();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SeekerOfSunlight());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Only the chosen creature receives the effects")
    void doesNotAffectOtherCreatures() {
        addMight();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SeekerOfSunlight());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SeekerOfSunlight());

        resolveBeginningOfCombat(player1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Combat proceeds without a target when no creatures are controlled")
    void noLegalTarget() {
        addMight();
        harness.addToBattlefield(player2, new SeekerOfSunlight());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private void addMight() {
        harness.addToBattlefield(player1, new MightOfTheAncestors());
    }

    private void resolveBeginningOfCombat(Player activePlayer, Permanent target) {
        advanceToBeginningOfCombat(activePlayer);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
