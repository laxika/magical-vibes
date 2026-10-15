package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GaleriderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThorncasterSliver.class, GaleriderSliver.class, GrizzlyBears.class})
class ThorncasterSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Thorncaster Sliver's own attack trigger deals 1 damage to any target")
    void ownAttackTriggerDealsDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ThorncasterSliver());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // 1 from the trigger, then 2 combat damage from the 2/2.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Another attacking Sliver gets the granted trigger and is the damage source")
    void otherSliverGetsGrantedTrigger() {
        addCreatureReady(player1, new GaleriderSliver());
        addCreatureReady(player1, new ThorncasterSliver());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        // Attack with the Galerider only; Thorncaster stays home.
        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the attack trigger")
    void nonSliverGetsNoTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new ThorncasterSliver());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granted trigger goes away when Thorncaster Sliver leaves the battlefield")
    void grantEndsWhenSourceLeaves() {
        addCreatureReady(player1, new GaleriderSliver());
        Permanent thorncaster = addCreatureReady(player1, new ThorncasterSliver());
        gd.playerBattlefields.get(player1.getId()).remove(thorncaster);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opposing Slivers do not gain the attack trigger")
    void opposingSliverGetsNoTrigger() {
        addCreatureReady(player1, new ThorncasterSliver());
        addCreatureReady(player2, new GaleriderSliver());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Two Thorncasters grant two independent triggers with different targets")
    void multipleCopiesGrantIndependentTriggers() {
        addCreatureReady(player1, new GaleriderSliver());
        addCreatureReady(player1, new ThorncasterSliver());
        addCreatureReady(player1, new ThorncasterSliver());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player1.getId());
            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();

            harness.assertLife(player1, 19);
            harness.assertLife(player2, 19);
        });
    }

    @Test
    @DisplayName("Every attacking Sliver triggers separately")
    void eachAttackingSliverTriggers() {
        addCreatureReady(player1, new GaleriderSliver());
        addCreatureReady(player1, new ThorncasterSliver());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, player2.getId());
            harness.handlePermanentChosen(player1, player2.getId());
            resolveAllTriggers();

            harness.assertLife(player2, 18);
        });
    }

    @Test
    @DisplayName("A granted trigger still resolves after Thorncaster leaves the battlefield")
    void grantedTriggerSurvivesGrantingSourceLeaving() {
        addCreatureReady(player1, new GaleriderSliver());
        Permanent thorncaster = addCreatureReady(player1, new ThorncasterSliver());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, player2.getId());
            gd.playerBattlefields.get(player1.getId()).remove(thorncaster);
            resolveAllTriggers();

            harness.assertLife(player2, 19);
        });
    }
}
