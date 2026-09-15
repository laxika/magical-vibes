package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlitzwingCruelTormentor.class, BlitzwingAdaptiveAssailant.class, Shock.class})
class BlitzwingCruelTormentorTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsBlitzwingConvertedWithLivingMetal() {
        Permanent blitzwing = castConvertedBlitzwing();

        assertThat(blitzwing.isTransformed()).isTrue();
        assertThat(blitzwing.getCard()).isInstanceOf(BlitzwingAdaptiveAssailant.class);
        assertThat(gqs.isCreature(gd, blitzwing)).isTrue();
    }

    @Test
    void beginningOfCombatGrantsOneRandomKeywordUntilEndOfTurn() {
        Permanent blitzwing = castConvertedBlitzwing();

        resolveBeginningOfCombat();

        boolean hasFlying = gqs.hasKeyword(gd, blitzwing, Keyword.FLYING);
        boolean hasIndestructible = gqs.hasKeyword(gd, blitzwing, Keyword.INDESTRUCTIBLE);
        assertThat(hasFlying ^ hasIndestructible).isTrue();
    }

    @Test
    void yourEndStepRepeatsOpponentsLifeLossWithoutConverting() {
        Permanent blitzwing = harness.addToBattlefieldAndReturn(player1, new BlitzwingCruelTormentor());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        advanceToEndStepAndChooseOpponent();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(blitzwing.isTransformed()).isFalse();
    }

    @Test
    void yourEndStepConvertsWhenTargetOpponentLostNoLife() {
        Permanent blitzwing = harness.addToBattlefieldAndReturn(player1, new BlitzwingCruelTormentor());
        harness.setLife(player2, 20);

        advanceToEndStepAndChooseOpponent();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(blitzwing.isTransformed()).isTrue();
    }

    @Test
    void combatDamageToAPlayerConvertsBlitzwingBack() {
        Permanent blitzwing = castConvertedBlitzwing();
        blitzwing.setAttacking(true);
        blitzwing.setAttackTarget(player2.getId());

        resolveCombat();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();

        assertThat(blitzwing.isTransformed()).isFalse();
        assertThat(blitzwing.getCard()).isInstanceOf(BlitzwingCruelTormentor.class);
    }

    private Permanent castConvertedBlitzwing() {
        harness.setHand(player1, List.of(new BlitzwingCruelTormentor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Blitzwing, Adaptive Assailant");
    }

    private void resolveBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStepAndChooseOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
    }
}
