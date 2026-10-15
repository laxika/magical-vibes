package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RangersGuile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolarianKraken.class, GrizzlyBears.class, RangersGuile.class})
class TolarianKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("After drawing, paying {1} and accepting taps a target creature")
    void paysAndTapsTargetCreature() {
        harness.addToBattlefield(player1, new TolarianKraken());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("After drawing, paying {1} and accepting untaps a target creature")
    void paysAndUntapsTargetCreature() {
        harness.addToBattlefield(player1, new TolarianKraken());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();

        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to pay leaves the target creature unchanged")
    void decliningPaymentDoesNothing() {
        harness.addToBattlefield(player1, new TolarianKraken());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Paying but declining the tap or untap leaves the target creature unchanged")
    void decliningTapOrUntapDoesNothing() {
        harness.addToBattlefield(player1, new TolarianKraken());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent drawing does not trigger the Kraken")
    void opponentDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new TolarianKraken());

        advanceToDraw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Without mana, accepting payment cannot create a reflexive trigger")
    void cannotPayWithoutMana() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new TolarianKraken());

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kraken.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A creature leaving before the reflexive trigger resolves is not affected")
    void targetLeavingBeforeResolutionDoesNothing() {
        harness.addToBattlefield(player1, new TolarianKraken());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TolarianKraken());

        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The reflexive trigger cannot target an opponent's hexproof creature")
    void excludesOpposingHexproofCreature() {
        Permanent ownKraken = harness.addToBattlefieldAndReturn(player1, new TolarianKraken());
        Permanent opposingKraken = harness.addToBattlefieldAndReturn(player2, new TolarianKraken());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new RangersGuile()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, opposingKraken.getId());
        harness.passBothPriorities();

        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ownKraken.getId()).doesNotContain(opposingKraken.getId());
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
