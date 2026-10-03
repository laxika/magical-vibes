package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MomentOfCraving;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlazingHope.class, CrawWurm.class, GrizzlyBears.class, MomentOfCraving.class})
class BlazingHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature whose power equals your life total")
    void exilesCreatureAtLifeTotal() {
        harness.setLife(player1, 6);
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setHand(player1, List.of(new BlazingHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Craw Wurm");
        harness.castAndResolveInstant(player1, 0, targetId);

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

    @Test
    @DisplayName("Can exile your own creature with power above your life total")
    void exilesOwnCreatureAboveLifeTotal() {
        harness.setLife(player1, 5);
        harness.addToBattlefield(player1, new CrawWurm());
        harness.setHand(player1, List.of(new BlazingHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Craw Wurm"));

        harness.assertNotOnBattlefield(player1, "Craw Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Craw Wurm"));
    }

    @Test
    @DisplayName("A decrease in your life total does not invalidate the target")
    void targetRemainsLegalAfterLifeLoss() {
        harness.setLife(player1, 6);
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setHand(player1, List.of(new BlazingHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Craw Wurm"));
        harness.setLife(player1, 5);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Craw Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Craw Wurm"));
    }

    @Test
    @DisplayName("Rechecks effective power after an opponent reduces it in response")
    void targetBecomesIllegalAfterPowerReduction() {
        harness.setLife(player1, 6);
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setHand(player1, List.of(new BlazingHope()));
        harness.setHand(player2, List.of(new MomentOfCraving()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Craw Wurm");
        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Craw Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new BlazingHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
