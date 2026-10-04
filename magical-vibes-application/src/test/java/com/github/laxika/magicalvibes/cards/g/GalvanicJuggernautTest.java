package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
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
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({GalvanicJuggernaut.class, CruelEdict.class, GrizzlyBears.class})
class GalvanicJuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Galvanic Juggernaut does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        juggernaut.tap();

        harness.performUntapStep(player1);

        assertThat(juggernaut.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Galvanic Juggernaut untaps when another creature dies")
    void untapsWhenAnotherCreatureDies() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        juggernaut.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve mandatory untap trigger

        assertThat(juggernaut.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Galvanic Juggernaut stays tapped when no creature dies")
    void staysTappedWhenNoOtherCreatureDies() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        juggernaut.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Opponent controls no creatures, so nothing dies and no trigger fires.
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(juggernaut.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declaring no attackers while Galvanic Juggernaut can attack throws 'must attack'")
    void mustAttackWhenAble() {
        addReadyJuggernaut(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void ownDeathDoesNotTriggerUntap() {
        addReadyJuggernaut(player1).tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Galvanic Juggernaut");
        harness.assertInGraveyard(player1, "Galvanic Juggernaut");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedJuggernautDoesNotHaveToAttack() {
        addReadyJuggernaut(player1).tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void summoningSickJuggernautDoesNotHaveToAttack() {
        Permanent juggernaut = addReadyJuggernaut(player1);
        juggernaut.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of()))
                .doesNotThrowAnyException();
    }

    private Permanent addReadyJuggernaut(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GalvanicJuggernaut());
        perm.setSummoningSick(false);
        return perm;
    }

}
