package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("scryfall")
class SpiritOfTheHearthTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameService gs;
    protected GameQueryService gqs;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
    }

    @Test
    @DisplayName("Controller has hexproof while Spirit of the Hearth is on the battlefield")
    void controllerHasHexproof() {
        harness.addToBattlefield(player1, new SpiritOfTheHearth());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target the controller with a spell")
    void opponentCannotTargetController() {
        harness.addToBattlefield(player1, new SpiritOfTheHearth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can still target themselves")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new SpiritOfTheHearth());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    @Test
    @DisplayName("Controller loses hexproof once Spirit of the Hearth leaves the battlefield")
    void hexproofGoneAfterSpiritLeaves() {
        SpiritOfTheHearth spirit = new SpiritOfTheHearth();
        harness.addToBattlefield(player1, spirit);

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spirit of the Hearth"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(spirit);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }
}
