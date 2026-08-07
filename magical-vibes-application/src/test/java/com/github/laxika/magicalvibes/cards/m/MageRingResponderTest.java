package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MageRingResponderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking deals 7 damage to the chosen defending creature")
    void attackTriggerDealsSevenDamage() {
        addResponderReady(player1);
        Permanent giant = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A creature the attacking player controls is not a legal target")
    void ownCreatureIsIllegalTarget() {
        addResponderReady(player1);
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No target selection when the defender controls no creatures")
    void noLegalTargetSkipsTrigger() {
        addResponderReady(player1);

        declareAttackers(List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Tapped Mage-Ring Responder does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent responder = addResponderReady(player1);
        responder.tap();

        advanceToNextTurn(player2);

        assertThat(responder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{7}: Untap this creature untaps it")
    void untapAbilityUntapsIt() {
        Permanent responder = addResponderReady(player1);
        responder.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(responder.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability cannot be activated without enough mana")
    void untapAbilityNeedsSevenMana() {
        Permanent responder = addResponderReady(player1);
        responder.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    private Permanent addResponderReady(Player player) {
        return addCreatureReady(player, new MageRingResponder());
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
