package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FiremaneAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion deals 3 damage to target player and gains 3 life")
    void battalionDamagesPlayerAndGainsLife() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        // 3 from the trigger, then 3 + 2 + 2 unblocked combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Battalion deals 3 damage to target creature")
    void battalionDamagesTargetCreature() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));

        harness.handlePermanentChosen(player1, opposing.getId());
        resolveAllTriggers();

        assertThat(opposing.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void noTriggerWithTooFewAttackers() {
        addCreatureReady(player1, new FiremaneAvenger());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Only the 3 + 2 unblocked combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
