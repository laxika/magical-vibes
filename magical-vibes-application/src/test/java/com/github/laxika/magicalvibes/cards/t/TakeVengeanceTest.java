package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakeVengeance.class, GreenwoodSentinel.class, Manalith.class})
class TakeVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target tapped creature")
    void destroysTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        target.tap();
        harness.setHand(player1, List.of(new TakeVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new TakeVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Can destroy a tapped creature you control")
    void destroysOwnTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        target.tap();
        harness.setHand(player1, List.of(new TakeVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetTappedNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Manalith());
        target.tap();
        harness.setHand(player1, List.of(new TakeVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Does not destroy a target that becomes untapped before resolution")
    void targetUntappedBeforeResolutionSurvives() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        target.tap();
        harness.setHand(player1, List.of(new TakeVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        harness.assertNotInGraveyard(player2, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Take Vengeance");
    }
}
