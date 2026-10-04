package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gutterbones.class, Shock.class})
class GutterbonesTest extends BaseCardTest {

    @Test
    @DisplayName("Gutterbones enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Gutterbones(), "{B}");
        harness.passBothPriorities();

        Permanent gutterbones = findPermanent(player1, "Gutterbones");
        assertThat(gutterbones.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated before an opponent loses life")
    void cannotActivateBeforeOpponentLosesLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Gutterbones()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated during an opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Gutterbones()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard ability returns Gutterbones to its owner's hand after an opponent loses life")
    void returnsFromGraveyardAfterOpponentLosesLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.setGraveyard(player1, List.of(new Gutterbones()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gutterbones");
        harness.assertNotInGraveyard(player1, "Gutterbones");
    }

    @Test
    @DisplayName("Life loss without damage qualifies even if the opponent subsequently gains life")
    void nonDamageLifeLossQualifiesAfterLifeGain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Gutterbones()));
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test");
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3);
        });
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gutterbones");
        harness.assertNotInGraveyard(player1, "Gutterbones");
    }

    @Test
    @DisplayName("Losing life yourself does not enable the graveyard ability")
    void controllersLifeLossDoesNotQualify() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Gutterbones()));
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, "test"));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Gutterbones");
        harness.assertNotInHand(player1, "Gutterbones");
    }

    @Test
    @DisplayName("An opponent's life loss does not permit activation on that opponent's turn")
    void opponentsTurnRemainsIllegalAfterLifeLoss() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Gutterbones()));
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can be activated during your end step and returns only its source")
    void endStepActivationReturnsOnlySourceCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        Gutterbones source = new Gutterbones();
        Gutterbones other = new Gutterbones();
        harness.setGraveyard(player1, List.of(source, other));
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(source).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("The graveyard ability requires both mana of its activation cost")
    void cannotActivateWithOnlyBlackMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new Gutterbones()));
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Gutterbones");
        harness.assertNotInHand(player1, "Gutterbones");
    }

    @Test
    @DisplayName("Multiple activations can be stacked but return the source only once")
    void multipleActivationsReturnSourceOnlyOnce() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Gutterbones source = new Gutterbones();
        harness.setGraveyard(player1, List.of(source));
        harness.inMutationScope(() ->
                harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "test"));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        harness.assertNotInGraveyard(player1, "Gutterbones");
        assertThat(gd.stack).isEmpty();
    }
}
