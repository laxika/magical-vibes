package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.s.SpinedThopter;
import com.github.laxika.magicalvibes.cards.m.MutagenicGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoxiousRevival.class, MutagenicGrowth.class, SpinedThopter.class})
class NoxiousRevivalTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting puts a graveyard-targeted instant on the stack")
    void castingPutsGraveyardTargetedInstantOnStack() {
        Card target = new MutagenicGrowth();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
        assertThat(entry.getTargetZone()).isEqualTo(Zone.GRAVEYARD);
    }

    @Test
    @DisplayName("Resolving puts targeted card from own graveyard on top of own library")
    void resolvePutsCardOnTopOfOwnLibrary() {
        Card target = new MutagenicGrowth();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Can target a card in opponent's graveyard and puts it on top of opponent's library")
    void canTargetOpponentGraveyardPutsOnOpponentLibrary() {
        Card opponentsCard = new SpinedThopter();
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, opponentsCard.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getId().equals(opponentsCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(opponentsCard.getId());
    }

    @Test
    @DisplayName("Can be cast by paying 2 life instead of green mana")
    void canBeCastWithPhyrexianMana() {
        Card target = new MutagenicGrowth();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new NoxiousRevival()));
        // No green mana — will pay with life

        harness.castAndResolveInstant(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Fizzles if targeted card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card target = new MutagenicGrowth();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Only the targeted card moves, preserving both libraries' order")
    void preservesOtherCardsAndLibraryOrder() {
        Card target = new SpinedThopter();
        Card otherGraveyardCard = new MutagenicGrowth();
        Card libraryTop = new MutagenicGrowth();
        Card libraryBottom = new SpinedThopter();
        Card ownLibraryCard = new MutagenicGrowth();
        harness.setGraveyard(player2, List.of(otherGraveyardCard, target));
        harness.setLibrary(player2, List.of(libraryTop, libraryBottom));
        harness.setLibrary(player1, List.of(ownLibraryCard));
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target, libraryTop, libraryBottom);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherGraveyardCard);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Noxious Revival");
    }

    @Test
    @DisplayName("Can put a card on top of an empty library")
    void canReturnCardToEmptyLibrary() {
        Card target = new MutagenicGrowth();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot cast without a target when both graveyards are empty")
    void cannotCastWithoutTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new NoxiousRevival()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Noxious Revival");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a card in hand")
    void cannotTargetCardOutsideGraveyard() {
        Card target = new MutagenicGrowth();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new NoxiousRevival(), target));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Noxious Revival");
        harness.assertInHand(player1, "Mutagenic Growth");
    }
}
