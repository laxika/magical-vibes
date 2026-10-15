package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrydleOfBaldursGate.class, Forest.class, HillGiantHerdgorger.class})
class KrydleOfBaldursGateTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the player lose life and mill, then gains life and scries")
    void combatDamageTriggersLifeLossMillLifeGainAndScry() {
        Permanent krydle = addCreatureReady(player1, new KrydleOfBaldursGate());
        krydle.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        int player1Life = gd.playerLifeTotals.get(player1.getId());
        int player2Life = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life - 2);
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Paying {2} after attacking makes a target creature unblockable")
    void payingTwoMakesTargetCreatureUnblockable() {
        addCreatureReady(player1, new KrydleOfBaldursGate());
        Permanent target = addCreatureReady(player2, new HillGiantHerdgorger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the attack payment leaves the target blockable")
    void decliningPaymentLeavesTargetBlockable() {
        addCreatureReady(player1, new KrydleOfBaldursGate());
        Permanent target = addCreatureReady(player2, new HillGiantHerdgorger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Attacking with multiple other creatures triggers once while Krydle stays back")
    void otherCreaturesAttackingTriggerOnce() {
        Permanent krydle = addCreatureReady(player1, new KrydleOfBaldursGate());
        Permanent firstAttacker = addCreatureReady(player1, new HillGiantHerdgorger());
        addCreatureReady(player1, new HillGiantHerdgorger());
        addCreatureReady(player2, new HillGiantHerdgorger());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        declareAttackers(List.of(1, 2));

        harness.handlePermanentChosen(player1, firstAttacker.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(firstAttacker.isCantBeBlocked()).isTrue();
        assertThat(krydle.isAttacking()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An empty damaged player's library does not prevent life gain or scry")
    void emptyOpponentLibraryDoesNotStopRemainingEffects() {
        Permanent krydle = addCreatureReady(player1, new KrydleOfBaldursGate());
        krydle.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of());
        int player1Life = gd.playerLifeTotals.get(player1.getId());
        int player2Life = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life - 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
