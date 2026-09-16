package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ScaledDestruction.class, GrizzlyBears.class, GiantSpider.class, SerraAngel.class,
        ColossalDreadmaw.class})
class ScaledDestructionTest extends BaseCardTest {

    @Test
    @DisplayName("Small mode destroys creatures with total power and toughness of 4 or less")
    void destroysSmallCreatures() {
        addAllSizes();

        cast(0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Medium mode destroys creatures with total power and toughness from 5 through 8")
    void destroysMediumCreatures() {
        addAllSizes();

        cast(1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Large mode destroys creatures with total power and toughness of 9 or more")
    void destroysLargeCreatures() {
        addAllSizes();

        cast(2);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("One or more modes may be chosen")
    void allowsChoosingAllModes() {
        addAllSizes();

        harness.setHand(player1, List.of(new ScaledDestruction()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0, 1, 2}, List.of(), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Colossal Dreadmaw");
    }

    private void addAllSizes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new ScaledDestruction()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{mode}, List.of(), List.of());
        harness.passBothPriorities();
    }
}
