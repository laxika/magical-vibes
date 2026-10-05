package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MageRingResponder.class, GrizzlyBears.class, HillGiant.class, Disperse.class})
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

        advanceToUpkeep(player1);

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

    @Test
    @DisplayName("Attack trigger still deals damage after its source leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent responder = addResponderReady(player1);
        Permanent target = addResponderReady(player2);
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player2, 0, responder.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Mage-Ring Responder");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Mage-Ring Responder");
    }

    @Test
    @DisplayName("Attack trigger does not damage another creature when its target leaves")
    void attackTriggerDoesNotRetarget() {
        addResponderReady(player1);
        Permanent target = addResponderReady(player2);
        Permanent other = addResponderReady(player2);
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player2, "Mage-Ring Responder");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attack damage is seven and can be prevented")
    void attackDamageRespectsPrevention() {
        addResponderReady(player1);
        Permanent target = addResponderReady(player2);
        target.setDamagePreventionShield(1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Mage-Ring Responder");
        assertThat(target.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Untap ability works with summoning sickness on an opponent's turn and untaps only itself")
    void untapAbilityWorksOnOpponentsTurnWithSummoningSickness() {
        Permanent responder = addResponderReady(player1);
        responder.setSummoningSick(true);
        responder.tap();
        Permanent other = addResponderReady(player1);
        other.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(responder.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    private Permanent addResponderReady(Player player) {
        return addCreatureReady(player, new MageRingResponder());
    }

}
