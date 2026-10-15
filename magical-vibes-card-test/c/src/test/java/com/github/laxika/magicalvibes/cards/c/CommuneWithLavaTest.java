package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommuneWithLava.class, Forest.class, GrizzlyBears.class, TimeWarp.class})
class CommuneWithLavaTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top X cards and grants play permission until the end of the next turn")
    void exilesTopXCardsAndGrantsPlayPermission() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 5);
        prepareMainPhase();

        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(first, second, third);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .containsEntry(third.getId(), player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    @DisplayName("Allows playing lands and casting creatures from the exiled cards")
    void playsAndCastsFromExile() {
        Card exiledLand = new Forest();
        Card exiledCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledLand, exiledCreature));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        gs.playCardFromExile(gd, player1, exiledLand.getId(), null, null);
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCardFromExile(gd, player1, exiledCreature.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void zeroXLeavesLibraryUnchanged() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 2);
        prepareMainPhase();

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Commune with Lava");
    }

    @Test
    void exilesOnlyAvailableCardsWhenXExceedsLibrarySize() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 5);
        prepareMainPhase();

        harness.castInstantForX(player1, 0, 3, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        harness.castFromExile(player1, top.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void permissionDoesNotWaiveManaCostsOrCreatureTiming() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase();
        harness.castInstantForX(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.END_STEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void permissionDoesNotGrantAdditionalLandPlays() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();
        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.castFromExile(player1, first.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void permissionLastsThroughNextTurnAndUnplayedCardsRemainExiled() {
        harness.setHand(player2, List.of());
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second,
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.addMana(player1, ManaColor.RED, 4);
        prepareMainPhase();
        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void permissionExpiresAtEndOfControllersExtraTurn() {
        harness.setHand(player2, List.of());
        Card exiledLand = new Forest();
        harness.setLibrary(player1, List.of(exiledLand,
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new CommuneWithLava(), new TimeWarp()));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase();
        harness.castInstantForX(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiledLand.getId());
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledLand);
    }

    @Test
    void opponentsExtraTurnDoesNotExpirePermissionBeforeControllersNextTurn() {
        Card exiledLand = new Forest();
        harness.setLibrary(player1, List.of(exiledLand,
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new CommuneWithLava()));
        harness.setHand(player2, List.of(new TimeWarp()));
        harness.addMana(player1, ManaColor.RED, 3);
        prepareMainPhase();
        harness.castInstantForX(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castSorcery(player2, 0, player2.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, exiledLand.getId());

        harness.assertOnBattlefield(player1, "Forest");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
