package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.StormscaleScion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsPrey.class, StormscaleScion.class, DalkovanPackbeasts.class})
class DragonsPreyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a non-Dragon creature for its normal cost")
    void destroysNonDragonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DalkovanPackbeasts());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dalkovan Packbeasts");
    }

    @Test
    @DisplayName("Costs two more when targeting a Dragon")
    void costsMoreWhenTargetingDragon() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormscaleScion());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Stormscale Scion");
    }

    @Test
    @DisplayName("Destroys a Dragon when the additional cost is paid")
    void destroysDragonWithAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormscaleScion());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stormscale Scion");
    }

    @Test
    @DisplayName("Four mana is still insufficient when targeting a Dragon")
    void cannotUnderpayDragonSurcharge() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormscaleScion());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        harness.assertInHand(player1, "Dragon's Prey");
        harness.assertOnBattlefield(player2, "Stormscale Scion");
    }

    @Test
    @DisplayName("The Dragon surcharge is generic and applies to your own Dragon")
    void destroysOwnDragonWithGenericSurcharge() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StormscaleScion());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stormscale Scion");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Paying the Dragon surcharge does not replace the required black mana")
    void stillRequiresBlackManaWhenTargetingDragon() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StormscaleScion());
        harness.setHand(player1, List.of(new DragonsPrey()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        harness.assertInHand(player1, "Dragon's Prey");
        harness.assertOnBattlefield(player2, "Stormscale Scion");
    }
}
