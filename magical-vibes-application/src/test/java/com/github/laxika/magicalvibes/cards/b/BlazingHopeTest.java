package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlazingHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature whose power equals your life total")
    void exilesCreatureAtLifeTotal() {
        harness.setLife(player1, 6);
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setHand(player1, List.of(new BlazingHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Craw Wurm");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Craw Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Craw Wurm"));
    }

    @Test
    @DisplayName("Cannot target a creature with power below your life total")
    void cannotTargetCreatureBelowLifeTotal() {
        harness.setLife(player1, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlazingHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rechecks your life total when the spell resolves")
    void rechecksLifeTotalAtResolution() {
        harness.setLife(player1, 6);
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setHand(player1, List.of(new BlazingHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Craw Wurm");
        harness.castInstant(player1, 0, targetId);
        harness.setLife(player1, 7);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Craw Wurm");
    }
}
