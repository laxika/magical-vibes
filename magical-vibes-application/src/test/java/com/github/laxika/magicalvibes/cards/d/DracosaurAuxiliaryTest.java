package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DracosaurAuxiliary.class, GrizzlyBears.class})
class DracosaurAuxiliaryTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled deals 2 damage to a target creature")
    void attacksWhileSaddledDealsDamageToCreature() {
        Permanent dracosaur = addCreatureReady(player1, new DracosaurAuxiliary());
        dracosaur.setSaddled(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking while saddled can deal 2 damage to a player")
    void attacksWhileSaddledDealsDamageToPlayer() {
        Permanent dracosaur = addCreatureReady(player1, new DracosaurAuxiliary());
        dracosaur.setSaddled(true);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent dracosaur = addCreatureReady(player1, new DracosaurAuxiliary());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        dracosaur.setSaddled(true);

        assertThat(gd.interaction.permanentChoiceContext()).isNull();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Saddle 3 taps another creature and enables the attack trigger")
    void saddleEnablesAttackTrigger() {
        Permanent dracosaur = addCreatureReady(player1, new DracosaurAuxiliary());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new DracosaurAuxiliary());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(dracosaur.isTapped()).isFalse();
        assertThat(dracosaur.isSaddled()).isTrue();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The saddled attack trigger still deals damage after its source leaves")
    void attackTriggerResolvesAfterSourceLeaves() {
        Permanent dracosaur = addCreatureReady(player1, new DracosaurAuxiliary());
        dracosaur.setSaddled(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(dracosaur);
        gd.playerGraveyards.get(player1.getId()).add(dracosaur.getCard());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The attack trigger can target another creature its controller controls")
    void attackTriggerCanTargetOwnCreature() {
        Permanent dracosaur = addCreatureReady(player1, new DracosaurAuxiliary());
        dracosaur.setSaddled(true);
        Permanent target = addCreatureReady(player1, new DracosaurAuxiliary());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }
}
