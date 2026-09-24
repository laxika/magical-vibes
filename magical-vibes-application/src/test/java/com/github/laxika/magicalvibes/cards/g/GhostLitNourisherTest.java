package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhostLitNourisher.class, HandOfHonor.class})
class GhostLitNourisherTest extends BaseCardTest {

    @Test
    @DisplayName("The battlefield ability gives a creature +2/+2 and taps Ghost-Lit Nourisher")
    void battlefieldAbilityBoostsTargetCreature() {
        Permanent nourisher = addCreatureReady(player1, new GhostLitNourisher());
        Permanent target = addCreatureReady(player1, new HandOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(nourisher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Channel ability gives a creature +4/+4 and discards Ghost-Lit Nourisher")
    void channelBoostsTargetCreatureAndDiscardsSource() {
        harness.setHand(player1, List.of(new GhostLitNourisher()));
        Permanent target = addCreatureReady(player1, new HandOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Ghost-Lit Nourisher");
    }

    @Test
    @DisplayName("The battlefield boost wears off at end of turn")
    void battlefieldBoostWearsOffAtEndOfTurn() {
        Permanent nourisher = addCreatureReady(player1, new GhostLitNourisher());
        Permanent target = addCreatureReady(player1, new HandOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(nourisher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Channel boost wears off at end of turn")
    void channelBoostWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new GhostLitNourisher()));
        Permanent target = addCreatureReady(player1, new HandOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The battlefield ability can target an opponent's creature")
    void battlefieldAbilityTargetsOpponentCreature() {
        addCreatureReady(player1, new GhostLitNourisher());
        Permanent target = addCreatureReady(player2, new HandOfHonor());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }
}
