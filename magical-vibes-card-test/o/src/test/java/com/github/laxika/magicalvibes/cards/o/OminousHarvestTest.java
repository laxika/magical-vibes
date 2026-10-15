package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OminousHarvest.class, Forest.class, GrizzlyBears.class, Shock.class})
class OminousHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws a card and loses 1 life")
    void targetPlayerDrawsAndLosesLife() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLife(player2, 20);
        castOminousHarvest(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Gravestorm creates one copy for each permanent put into a graveyard from the battlefield")
    void gravestormCreatesCopiesForPermanentsPutIntoGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearId);

        castOminousHarvest(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new OminousHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayTargetItsController() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        castOminousHarvest(player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyMayDrawAndLoseLifeForADifferentPlayer() {
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castOminousHarvest(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsDeathInResponseAndCopyKeepsOriginalTargetWhenDeclined() {
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        castOminousHarvest(player2.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvedInstantDoesNotCountAsAPermanentDeath() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        castOminousHarvest(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    private void castOminousHarvest(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new OminousHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
