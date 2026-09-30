package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AetherMembrane;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoomBust.class, UrborgTombOfYawgmoth.class})
class BoomBustTest extends BaseCardTest {

    @Test
    @DisplayName("Boom destroys one land you control and one land you don't control")
    void boomDestroysOneLandOnEachSide() {
        var ownLand = harness.addToBattlefieldAndReturn(player1, new UrborgTombOfYawgmoth());
        var opposingLand = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());

        harness.setHand(player1, List.of(new BoomBust()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 0, List.of(ownLand.getId(), opposingLand.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Boom cannot target a land you control as the second target")
    void boomRequiresTheSecondLandToBeOutsideYourControl() {
        var ownLand = harness.addToBattlefieldAndReturn(player1, new UrborgTombOfYawgmoth());

        harness.setHand(player1, List.of(new BoomBust()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 0, List.of(ownLand.getId(), ownLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(AetherMembrane.class)
    @DisplayName("Boom cannot target a nonland permanent")
    void boomRequiresBothTargetsToBeLands() {
        var opposingLand = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());
        var opposingCreature = harness.addToBattlefieldAndReturn(player2, new AetherMembrane());

        harness.setHand(player1, List.of(new BoomBust()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 0, List.of(opposingCreature.getId(), opposingLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(AetherMembrane.class)
    @DisplayName("Bust destroys all lands but not non-land permanents")
    void bustDestroysAllLands() {
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());
        harness.addToBattlefield(player2, new UrborgTombOfYawgmoth());
        harness.addToBattlefield(player2, new AetherMembrane());

        harness.setHand(player1, List.of(new BoomBust()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player2, "Urborg, Tomb of Yawgmoth");
        harness.assertOnBattlefield(player2, "Aether Membrane");
    }
}
