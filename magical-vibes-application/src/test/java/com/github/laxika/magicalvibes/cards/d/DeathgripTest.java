package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deathgrip.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class})
class DeathgripTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target green spell")
    void countersGreenSpell() {
        Permanent deathgrip = harness.addToBattlefieldAndReturn(player1, new Deathgrip());
        harness.addMana(player1, ManaColor.BLACK, 2);

        GrizzlyBears bears = new GrizzlyBears();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot counter a non-green spell")
    void cannotTargetNonGreenSpell() {
        harness.addToBattlefield(player1, new Deathgrip());
        harness.addMana(player1, ManaColor.BLACK, 2);

        HillGiant giant = new HillGiant();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, giant, "{3}{R}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a green instant spell")
    void countersGreenInstantSpell() {
        harness.addToBattlefield(player1, new Deathgrip());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player2, List.of(growth));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, growth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Growth");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without two black mana")
    void cannotActivateWithoutTwoBlackMana() {
        harness.addToBattlefield(player1, new Deathgrip());
        GrizzlyBears bears = new GrizzlyBears();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
