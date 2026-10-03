package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonOfCatastrophes.class, GreenwoodSentinel.class})
class DemonOfCatastrophesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature as an additional cost and enters the battlefield")
    void sacrificesCreatureAsAdditionalCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.setHand(player1, List.of(new DemonOfCatastrophes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Demon of Catastrophes");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new DemonOfCatastrophes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Sacrifice is paid before the spell resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new DemonOfCatastrophes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertNotOnBattlefield(player1, "Demon of Catastrophes");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Demon of Catastrophes");
    }

    @Test
    @DisplayName("A tapped creature can pay the sacrifice cost")
    void canSacrificeTappedCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        sacrifice.setTapped(true);
        harness.setHand(player1, List.of(new DemonOfCatastrophes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Demon of Catastrophes");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentsCreature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new DemonOfCatastrophes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentsCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInHand(player1, "Demon of Catastrophes");
    }
}
