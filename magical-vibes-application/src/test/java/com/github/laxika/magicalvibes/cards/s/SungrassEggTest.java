package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SungrassEgg.class)
class SungrassEggTest extends BaseCardTest {

    @Test
    void activationSacrificesAddsBothManaAndDraws() {
        harness.addToBattlefield(player1, new SungrassEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Sungrass Egg");
        harness.assertInGraveyard(player1, "Sungrass Egg");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresTwoGenericMana() {
        harness.addToBattlefield(player1, new SungrassEgg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Sungrass Egg");
    }

    @Test
    void activationRequiresAnUntappedSource() {
        var egg = harness.addToBattlefieldAndReturn(player1, new SungrassEgg());
        egg.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, null));

        harness.assertOnBattlefield(player1, "Sungrass Egg");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
