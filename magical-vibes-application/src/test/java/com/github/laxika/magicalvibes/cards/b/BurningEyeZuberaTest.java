package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurningEyeZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target when it dies after being dealt 4 damage")
    void dealsThreeDamageAfterBeingDealtFourDamage() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new BurningEyeZubera());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player2, 0, zubera.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Burning-Eye Zubera");
    }

    @Test
    @DisplayName("Does not deal damage when it dies after being dealt less than 4 damage")
    void doesNotDealDamageAfterBeingDealtLessThanFourDamage() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new BurningEyeZubera());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock(), new DiabolicEdict()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, zubera.getId());
        harness.passBothPriorities();

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Burning-Eye Zubera");
    }
}
