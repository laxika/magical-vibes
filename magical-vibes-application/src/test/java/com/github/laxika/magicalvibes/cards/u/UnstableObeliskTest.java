package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnstableObelisk.class, GrizzlyBears.class, Island.class})
class UnstableObeliskTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Unstable Obelisk adds {C}")
    void tapsForColorlessMana() {
        Permanent obelisk = addReadyObelisk(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(obelisk.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("{7}, {T}, Sacrifice: destroys target permanent")
    void sacrificesAndDestroysTargetPermanent() {
        addReadyObelisk(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Unstable Obelisk");
        harness.assertInGraveyard(player1, "Unstable Obelisk");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the destruction ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyObelisk(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The destruction ability can target a land")
    void canDestroyLand() {
        addReadyObelisk(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player2, "Island");
    }

    private Permanent addReadyObelisk(com.github.laxika.magicalvibes.model.Player player) {
        Permanent obelisk = new Permanent(new UnstableObelisk());
        obelisk.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(obelisk);
        return obelisk;
    }
}
