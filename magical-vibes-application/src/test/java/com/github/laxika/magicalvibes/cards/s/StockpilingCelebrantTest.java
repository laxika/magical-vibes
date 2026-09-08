package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StockpilingCelebrant.class, GrizzlyBears.class, Island.class})
class StockpilingCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Returning another nonland permanent scries 2")
    void returnsPermanentAndScriesTwo() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castCelebrant(List.of(bears.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Stockpiling Celebrant");
    }

    @Test
    @DisplayName("Declining the optional return does not scry")
    void canDeclineReturn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castCelebrant(List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Stockpiling Celebrant");
    }

    @Test
    @DisplayName("A land cannot be targeted")
    void rejectsLandTarget() {
        var island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new StockpilingCelebrant()));
        addCelebrantMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonland permanent you control");
    }

    @Test
    @DisplayName("An opponent's permanent cannot be targeted")
    void rejectsOpponentPermanentTarget() {
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StockpilingCelebrant()));
        addCelebrantMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonland permanent you control");
    }

    private void castCelebrant(List<UUID> targets) {
        harness.setHand(player1, List.of(new StockpilingCelebrant()));
        addCelebrantMana();
        if (targets.isEmpty()) {
            harness.castCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0, 0, targets.getFirst());
        }
    }

    private void addCelebrantMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
