package com.github.laxika.magicalvibes.cards.c;

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

class CrumbleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crumble destroys target artifact and its controller gains life equal to its mana value")
    void destroysArtifactAndControllerGainsLife() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new Crumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int casterLifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int ownerLifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rod of Ruin"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
        // Rod of Ruin has mana value 4; its controller (player2), not the caster, gains 4 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLifeBefore + 4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore);
    }

    @Test
    @DisplayName("Cannot target a creature with Crumble")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Crumble()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
