package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.r.Rescue;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Donate.class, GoliathBeetle.class, YavimayaHollow.class, Rescue.class})
class DonateTest extends BaseCardTest {

    @Test
    @DisplayName("Target player gains control of target permanent you control")
    void targetPlayerGainsControlOfTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
        harness.assertOnBattlefield(player2, "Goliath Beetle");
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetYourself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player1.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goliath Beetle");
        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
    }

    @Test
    @DisplayName("Can target a noncreature permanent you control")
    void canTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yavimaya Hollow");
        harness.assertOnBattlefield(player2, "Yavimaya Hollow");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by another player")
    void cannotTargetPermanentControlledByAnotherPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoliathBeetle());
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Control does not expire at end of turn and does not untap the permanent")
    void controlPersistsAndPreservesTappedState() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        target.setTapped(true);
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new Donate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.isSummoningSick()).isTrue();
        harness.passUntil(TurnStep.UPKEEP);
        harness.assertOnBattlefield(player2, "Goliath Beetle");
        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
    }

    @Test
    @DisplayName("A permanent returned to hand in response is not donated")
    void permanentLeavingBattlefieldInResponseIsNotDonated() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Donate(), new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Goliath Beetle");
        harness.assertNotInHand(player2, "Goliath Beetle");
        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
        harness.assertInGraveyard(player1, "Donate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Donation changes control but not ownership")
    void donatedPermanentReturnsToOriginalOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoliathBeetle());
        harness.setHand(player1, List.of(new Donate()));
        harness.setHand(player2, List.of(new Rescue()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Goliath Beetle");
        harness.assertNotInHand(player2, "Goliath Beetle");
        harness.assertNotOnBattlefield(player1, "Goliath Beetle");
        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
    }
}
