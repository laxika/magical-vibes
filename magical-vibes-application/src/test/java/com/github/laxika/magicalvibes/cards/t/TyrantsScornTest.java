package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TyrantsScorn.class, GrizzlyBears.class, AirElemental.class})
class TyrantsScornTest extends BaseCardTest {

    @Test
    @DisplayName("Destroy mode destroys a creature with mana value 3 or less")
    void destroyModeDestroysLowManaValueCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        cast(0, "Grizzly Bears");

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Bounce mode returns any target creature to its owner's hand")
    void bounceModeReturnsHighManaValueCreatureToHand() {
        harness.addToBattlefield(player2, new AirElemental());
        cast(1, "Air Elemental");

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInHand(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Destroy mode cannot target a creature with mana value greater than 3")
    void destroyModeCannotTargetHighManaValueCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TyrantsScorn()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, String targetName) {
        harness.setHand(player1, List.of(new TyrantsScorn()));
        addMana();
        harness.castInstant(player1, 0, mode, harness.getPermanentId(player2, targetName));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
