package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InterplanarBeacon.class, JaceBeleren.class, Shock.class})
class InterplanarBeaconTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void differentColorManaCastsPlaneswalkerAndGainsLife() {
        harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        harness.setHand(player1, List.of(new JaceBeleren()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Jace Beleren");
    }

    @Test
    void planeswalkerManaCannotCastNonPlaneswalkerSpell() {
        harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        harness.setHand(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
