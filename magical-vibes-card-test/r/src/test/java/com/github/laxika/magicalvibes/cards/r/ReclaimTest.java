package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reclaim.class, GrizzlyBears.class})
class ReclaimTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts targeted card from own graveyard on top of own library")
    void resolvePutsCardOnTopOfOwnLibrary() {
        Card target = new GrizzlyBears();
        Card existingTop = new Reclaim();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(existingTop));
        harness.setHand(player1, List.of(new Reclaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(target.getId());
        assertThat(gd.playerDecks.get(player1.getId()).get(1).getId()).isEqualTo(existingTop.getId());
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card opponentsCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new Reclaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentsCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getId().equals(opponentsCard.getId()));
    }

    @Test
    @DisplayName("Cannot cast without a card in your graveyard to target")
    void cannotCastWithoutCardInOwnGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Reclaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if targeted card leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Reclaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(target.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Returns only the targeted noncreature card, leaving other graveyard cards alone")
    void returnsOnlyTargetedNoncreatureCard() {
        Card target = new Reclaim();
        Card other = new GrizzlyBears();
        Card existingTop = new Reclaim();
        Card spell = new Reclaim();
        harness.setGraveyard(player1, List.of(other, target));
        harness.setLibrary(player1, List.of(existingTop));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target, existingTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can put a card on top of an empty library")
    void returnsCardToEmptyLibrary() {
        Card target = new Reclaim();
        Card spell = new Reclaim();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }
}
