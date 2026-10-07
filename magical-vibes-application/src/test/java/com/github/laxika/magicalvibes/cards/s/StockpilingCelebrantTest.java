package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TempestHart;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
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

@CardUsed({StockpilingCelebrant.class, TempestHart.class, ScanTheClouds.class, Island.class, PropheticPrism.class})
class StockpilingCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Returning another nonland permanent scries 2")
    void returnsPermanentAndScriesTwo() {
        var hart = harness.addToBattlefieldAndReturn(player1, new TempestHart());
        castCelebrant(List.of(hart.getId()));

        resolveAllTriggers();
        acceptReturn();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);

        harness.assertInHand(player1, "Tempest Hart");
        harness.assertOnBattlefield(player1, "Stockpiling Celebrant");
    }

    @Test
    @DisplayName("Declining the optional return does not scry")
    void canDeclineReturn() {
        var hart = harness.addToBattlefieldAndReturn(player1, new TempestHart());
        castCelebrant(List.of(hart.getId()));

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.Scry.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Tempest Hart");
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
        var hart = harness.addToBattlefieldAndReturn(player2, new TempestHart());
        harness.setHand(player1, List.of(new StockpilingCelebrant()));
        addCelebrantMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, hart.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonland permanent you control");
    }

    private void castCelebrant(List<UUID> targets) {
        if (targets.isEmpty()) {
            harness.castFromHand(player1, new StockpilingCelebrant(), "{2}{W}");
            return;
        }
        harness.setHand(player1, List.of(new StockpilingCelebrant()));
        addCelebrantMana();
        harness.castCreature(player1, 0, targets.getFirst());
    }

    @Test
    @DisplayName("Entering with no other permanent does not return itself or scry")
    void noOtherPermanentDoesNotScry() {
        castCelebrant(List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Stockpiling Celebrant");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The return can target an artifact")
    void returnsArtifactAndScries() {
        var prism = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        castCelebrant(List.of(prism.getId()));
        resolveAllTriggers();
        acceptReturn();

        harness.assertInHand(player1, "Prophetic Prism");
        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("A target that leaves before resolution does not cause scry")
    void removedTargetDoesNotScry() {
        var celebrant = harness.addToBattlefieldAndReturn(player1, new StockpilingCelebrant());
        castCelebrant(List.of(celebrant.getId()));
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToHand(gd, celebrant);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Stockpiling Celebrant");
        assertThat(countPermanents(player1, "Stockpiling Celebrant")).isEqualTo(1);
    }

    @Test
    @DisplayName("Another copy of Stockpiling Celebrant is a legal target")
    void returnsAnotherCelebrant() {
        var celebrant = harness.addToBattlefieldAndReturn(player1, new StockpilingCelebrant());
        castCelebrant(List.of(celebrant.getId()));
        resolveAllTriggers();
        acceptReturn();

        harness.assertInHand(player1, "Stockpiling Celebrant");
        assertThat(countPermanents(player1, "Stockpiling Celebrant")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    private void acceptReturn() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void addCelebrantMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
