package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
