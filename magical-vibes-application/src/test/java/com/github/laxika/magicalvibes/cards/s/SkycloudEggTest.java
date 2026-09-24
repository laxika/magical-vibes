package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SkycloudEgg.class)
class SkycloudEggTest extends BaseCardTest {

    @Test
    void activationSacrificesAddsBothManaAndDraws() {
        harness.addToBattlefield(player1, new SkycloudEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Skycloud Egg");
        harness.assertInGraveyard(player1, "Skycloud Egg");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresTwoGenericMana() {
        harness.addToBattlefield(player1, new SkycloudEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Skycloud Egg");
    }

    @Test
    void activationRequiresAnUntappedSource() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new SkycloudEgg());
        egg.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Skycloud Egg");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void activationCanBeUsedWhileAnotherActivationIsOnTheStack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SkycloudEgg(), new SkycloudEgg()));
        harness.addToBattlefield(player1, new SkycloudEgg());
        harness.addToBattlefield(player1, new SkycloudEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
