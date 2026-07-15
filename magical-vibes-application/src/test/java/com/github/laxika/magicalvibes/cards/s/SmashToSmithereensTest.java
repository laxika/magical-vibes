package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmashToSmithereensTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and deals 3 damage to its controller")
    void destroysArtifactAndDealsDamage() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rod of Ruin"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Deals damage to controller even when artifact is indestructible")
    void dealsDamageEvenWhenIndestructible() {
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Plate");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Darksteel Plate is indestructible, still on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Darksteel Plate"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.RED, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
