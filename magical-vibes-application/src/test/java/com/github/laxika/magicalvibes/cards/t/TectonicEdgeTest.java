package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TectonicEdge.class, Forest.class, GhostQuarter.class})
class TectonicEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new TectonicEdge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate while opponents control fewer than four lands")
    void cannotActivateWithoutFourOpponentLands() {
        harness.addToBattlefield(player1, new TectonicEdge());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more lands");
    }

    @Test
    @DisplayName("At exactly four opponent lands, destroys a nonbasic land and sacrifices itself")
    void destroysNonbasicLandAtFourOpponentLands() {
        harness.addToBattlefield(player1, new TectonicEdge());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        harness.activateAbility(player1, 0, 1, null, targetId);

        GameData gameData = harness.getGameData();
        harness.assertInGraveyard(player1, "Tectonic Edge");
        assertThat(gameData.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Can target its controller's nonbasic land")
    void canTargetOwnNonbasicLand() {
        harness.addToBattlefield(player1, new TectonicEdge());
        harness.addToBattlefield(player1, new GhostQuarter());
        addFourLandsToOpponent();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player1, "Ghost Quarter");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost Quarter");
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new TectonicEdge());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both activations resolve even after the opponent falls below four lands")
    void landCountIsCheckedOnlyAtActivation() {
        harness.addToBattlefield(player1, new TectonicEdge());
        harness.addToBattlefield(player1, new TectonicEdge());
        UUID firstTarget = harness.addToBattlefieldAndReturn(player2, new TectonicEdge()).getId();
        UUID secondTarget = harness.addToBattlefieldAndReturn(player2, new TectonicEdge()).getId();
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, firstTarget);
        harness.activateAbility(player1, 0, 1, null, secondTarget);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate destruction without paying one mana")
    void requiresManaPayment() {
        harness.addToBattlefield(player1, new TectonicEdge());
        addFourLandsToOpponent();
        UUID targetId = harness.getPermanentId(player1, "Tectonic Edge");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tectonic Edge");
        harness.assertNotInGraveyard(player1, "Tectonic Edge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the tap cost with an already tapped Tectonic Edge")
    void requiresUntappedSource() {
        harness.addToBattlefield(player1, new TectonicEdge());
        addFourLandsToOpponent();
        harness.activateAbility(player1, 0, 0, null, null);
        UUID targetId = harness.getPermanentId(player1, "Tectonic Edge");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tectonic Edge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target itself, sacrificing it before the ability resolves")
    void canTargetItself() {
        harness.addToBattlefield(player1, new TectonicEdge());
        addFourLandsToOpponent();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player1, "Tectonic Edge");

        harness.activateAbility(player1, 0, 1, null, targetId);

        harness.assertNotOnBattlefield(player1, "Tectonic Edge");
        harness.assertInGraveyard(player1, "Tectonic Edge");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(4);
    }

    private void addFourLandsToOpponent() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
    }
}
