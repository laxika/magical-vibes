package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustsporeRam.class, LeoninScimitar.class, CopperMyr.class})
class RustsporeRamTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target Equipment")
    void etbDestroysTargetEquipment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new RustsporeRam()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rustspore Ram");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Cannot target a non-Equipment permanent")
    void cannotTargetNonEquipment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        harness.setHand(player1, List.of(new RustsporeRam()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Equipment");
    }

    @Test
    @DisplayName("ETB does not trigger without a legal Equipment target")
    void etbDoesNotTriggerWithoutLegalTarget() {
        harness.addToBattlefield(player2, new CopperMyr());
        harness.setHand(player1, List.of(new RustsporeRam()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rustspore Ram");
    }

    @Test
    @DisplayName("ETB fizzles if the target Equipment leaves before resolution")
    void etbFizzlesIfTargetEquipmentLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new RustsporeRam()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());

        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertOnBattlefield(player1, "Rustspore Ram");
    }
}
