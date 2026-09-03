package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thermokarst.class, Forest.class, SnowCoveredForest.class, BalduvianBears.class})
class ThermokarstTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonsnow land without gaining life")
    void destroysNonsnowLandNoLifeGain() {
        harness.addToBattlefield(player2, new Forest());
        castThermokarst(harness.getPermanentId(player2, "Forest"));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroys snow land and gains 1 life")
    void destroysSnowLandAndGainsLife() {
        Permanent snow = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        castThermokarst(snow.getId());

        harness.assertNotOnBattlefield(player2, "Snow-Covered Forest");
        harness.assertInGraveyard(player2, "Snow-Covered Forest");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life if the target is no longer snow when Thermokarst resolves")
    void doesNotGainLifeIfTargetIsNoLongerSnowAtResolution() {
        Permanent snow = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        harness.setHand(player1, List.of(new Thermokarst()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, snow.getId());

        TestCards.mutableCard(snow).setSupertypes(EnumSet.of(CardSupertype.BASIC));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snow-Covered Forest");
        harness.assertInGraveyard(player2, "Snow-Covered Forest");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new Thermokarst()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Balduvian Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private void castThermokarst(UUID targetId) {
        harness.setHand(player1, List.of(new Thermokarst()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0, targetId);
    }
}
