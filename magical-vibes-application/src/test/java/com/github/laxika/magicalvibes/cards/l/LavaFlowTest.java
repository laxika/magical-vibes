package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LavaFlowTest extends BaseCardTest {

    @Test
    @DisplayName("Lava Flow destroys target creature")
    void destroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Lava Flow destroys target land")
    void destroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, 0, forestId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Lava Flow cannot target a noncreature, nonland permanent")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new AuraOfSilence());
        harness.setHand(player1, List.of(new LavaFlow()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID auraId = harness.getPermanentId(player2, "Aura of Silence");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, auraId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or land");
    }
}
