package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.m.MidnightRecovery;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.cards.g.GutterSkulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercityInformer.class, GutterSkulk.class, MidnightRecovery.class, SimicGuildgate.class})
class UndercityInformerTest extends BaseCardTest {

    @Test
    @DisplayName("Mills the target player until a land is revealed, including the land")
    void millsUntilFirstLand() {
        addCreatureReady(player1, new UndercityInformer());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setLibrary(player2, List.of(
                new GutterSkulk(),
                new MidnightRecovery(),
                new SimicGuildgate(),      // stop here
                new GutterSkulk() // stays in library
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Gutter Skulk", "Midnight Recovery", "Simic Guildgate");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Gutter Skulk");

        // The Informer is the only creature, so it pays its own sacrifice cost.
        harness.assertInGraveyard(player1, "Undercity Informer");
    }

    @Test
    @DisplayName("A library with no land is entirely milled")
    void millsWholeLibraryWithoutLand() {
        addCreatureReady(player1, new UndercityInformer());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setLibrary(player2, List.of(
                new GutterSkulk(),
                new MidnightRecovery()
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactlyInAnyOrder("Gutter Skulk", "Midnight Recovery");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetSelf() {
        addCreatureReady(player1, new UndercityInformer());
        harness.addToBattlefield(player1, new GutterSkulk());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setLibrary(player1, List.of(new MidnightRecovery(), new SimicGuildgate()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gutter Skulk"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting("name")
                .contains("Midnight Recovery", "Simic Guildgate", "Gutter Skulk");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Undercity Informer");
    }

    @Test
    @DisplayName("Cannot activate without the source permanent")
    void requiresSourcePermanent() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the {1}")
    void requiresMana() {
        addCreatureReady(player1, new UndercityInformer());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land on top is the only card put into the graveyard")
    void stopsAtLandOnTop() {
        harness.addToBattlefield(player1, new UndercityInformer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player2, List.of(new SimicGuildgate(), new MidnightRecovery()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Undercity Informer");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactly("Simic Guildgate");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Midnight Recovery");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick during the opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent informer = harness.addToBattlefieldAndReturn(player1, new UndercityInformer());
        informer.setSummoningSick(true);
        informer.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player2, List.of(new MidnightRecovery(), new SimicGuildgate()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Undercity Informer");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactlyInAnyOrder("Midnight Recovery", "Simic Guildgate");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library reveals and moves no cards")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new UndercityInformer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Undercity Informer");
        assertThat(gd.stack).isEmpty();
    }
}
