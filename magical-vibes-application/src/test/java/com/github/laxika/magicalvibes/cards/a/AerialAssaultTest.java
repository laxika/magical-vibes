package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.SnappingDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerialAssault.class, GreenwoodSentinel.class, SnappingDrake.class})
class AerialAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature and gains life for each flying creature you control")
    void destroysTappedCreatureAndGainsLifeForControlledFliers() {
        harness.addToBattlefield(player1, new SnappingDrake());
        harness.addToBattlefield(player1, new SnappingDrake());
        harness.addToBattlefield(player2, new SnappingDrake());
        harness.addToBattlefield(player1, new GreenwoodSentinel());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        target.tap();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AerialAssault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent validTarget = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        validTarget.tap();
        Permanent untappedTarget = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.setHand(player1, List.of(new AerialAssault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, untappedTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Counts flying creatures after destroying your own tapped flier")
    void excludesDestroyedControlledFlierFromLifeGain() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SnappingDrake());
        target.tap();
        harness.addToBattlefield(player1, new SnappingDrake());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AerialAssault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snapping Drake");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains no life if the target becomes untapped before resolution")
    void illegalTargetPreventsLifeGain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        target.tap();
        harness.addToBattlefield(player1, new SnappingDrake());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AerialAssault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Aerial Assault");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroys the target even when you control no flying creatures")
    void destroysTargetWithoutLifeGain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        target.tap();
        harness.addToBattlefield(player2, new SnappingDrake());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AerialAssault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 20);
    }
}
