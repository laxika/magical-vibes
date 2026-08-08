package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PutrefyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Putrefy destroys a target creature and it can't be regenerated")
    void destroysTargetCreatureIgnoringRegeneration() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving Putrefy destroys a target artifact")
    void destroysTargetArtifact() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID thopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castInstant(player1, 0, thopterId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Putrefy cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID landId = harness.getPermanentId(player2, "Mountain");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
