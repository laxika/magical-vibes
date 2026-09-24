package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstableObelisk.class, Forest.class, GrizzlyBears.class})
class UnstableObeliskTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new UnstableObelisk());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(obelisk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the Obelisk destroys any target permanent")
    void sacrificesAndDestroysTargetPermanent() {
        harness.addToBattlefield(player1, new UnstableObelisk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Unstable Obelisk");
        harness.assertInGraveyard(player1, "Unstable Obelisk");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy a land")
    void canDestroyLand() {
        harness.addToBattlefield(player1, new UnstableObelisk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot activate the destruction ability without seven mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new UnstableObelisk());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
