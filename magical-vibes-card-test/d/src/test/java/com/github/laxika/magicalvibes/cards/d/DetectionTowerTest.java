package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.k.KnightOfGrace;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.v.VineMare;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DetectionTower.class, CarnageTyrant.class, LeylineOfSanctity.class, Shock.class, KnightOfGrace.class, Murder.class, VineMare.class})
class DetectionTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new DetectionTower());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its controller can target an opponent with hexproof")
    void controllerCanTargetOpponentWithHexproof() {
        harness.addToBattlefield(player1, new DetectionTower());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        activateHexproofIgnore(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Its controller can target an opponent's hexproof creature")
    void controllerCanTargetOpponentHexproofCreature() {
        harness.addToBattlefield(player1, new DetectionTower());
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        activateHexproofIgnore(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, tyrant.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Carnage Tyrant");
    }

    @Test
    @DisplayName("Without activating it, an opponent with hexproof cannot be targeted")
    void opponentHexproofStillBlocksWithoutActivation() {
        harness.addToBattlefield(player1, new DetectionTower());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void bypassesHexproofFromBlack() {
        harness.addToBattlefield(player1, new DetectionTower());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfGrace());
        activateHexproofIgnore(player1);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, knight.getId());

        harness.assertNotOnBattlefield(player2, "Knight of Grace");
        harness.assertInGraveyard(player2, "Knight of Grace");
    }

    @Test
    void appliesToCreaturesEnteringAfterResolution() {
        harness.addToBattlefield(player1, new DetectionTower());
        activateHexproofIgnore(player1);
        Permanent mare = harness.addToBattlefieldAndReturn(player2, new VineMare());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, mare.getId());

        harness.assertInGraveyard(player2, "Vine Mare");
    }

    @Test
    void doesNotLetOpponentIgnoreHexproof() {
        harness.addToBattlefield(player1, new DetectionTower());
        Permanent mare = harness.addToBattlefieldAndReturn(player1, new VineMare());
        activateHexproofIgnore(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, mare.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void permissionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new DetectionTower());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        Permanent mare = harness.addToBattlefieldAndReturn(player2, new VineMare());
        activateHexproofIgnore(player1);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, mare.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
    private void activateHexproofIgnore(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.activateAbility(player, 0, 1, null, null);
        harness.passBothPriorities();
    }
}
