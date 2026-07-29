package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmuletOfUnmakingTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability exiles the target creature")
    void exilesTargetCreature() {
        addReadyAmulet(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile a land")
    void exilesTargetLand() {
        addReadyAmulet(player1);
        harness.addToBattlefield(player2, new Plains());
        Permanent land = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Amulet is exiled as a cost, not sacrificed")
    void amuletExiledAsCost() {
        addReadyAmulet(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Amulet of Unmaking");
        harness.assertNotInGraveyard(player1, "Amulet of Unmaking");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Amulet of Unmaking"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyAmulet(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate during the opponent's turn (sorcery speed only)")
    void cannotActivateAtInstantSpeed() {
        addReadyAmulet(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyAmulet(Player player) {
        Permanent perm = new Permanent(new AmuletOfUnmaking());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
