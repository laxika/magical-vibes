package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PillageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Pillage destroys a target artifact and it can't be regenerated")
    void destroysTargetArtifact() {
        Permanent thopter = new Permanent(new Ornithopter());
        thopter.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(thopter);

        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, thopter.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ornithopter"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Resolving Pillage destroys a target land")
    void destroysTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, landId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Mountain"));
    }

    @Test
    @DisplayName("Pillage cannot target a nonartifact creature")
    void cannotTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Pillage()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
