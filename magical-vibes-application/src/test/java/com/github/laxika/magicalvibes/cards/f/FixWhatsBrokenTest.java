package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixWhatsBrokenTest extends BaseCardTest {

    @Test
    @DisplayName("Pays X life and returns every own artifact or creature with mana value X")
    void returnsMatchingArtifactsAndCreatures() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(),
                new CopperMyr(),
                new LeoninScimitar(),
                new LlanowarElves()));
        harness.setHand(player1, List.of(new FixWhatsBroken()));
        harness.setLife(player1, 20);
        addManaForSpell();

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Copper Myr")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Leonin Scimitar", "Llanowar Elves", "Fix What's Broken");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not return cards from another graveyard or cards with another type or mana value")
    void ignoresNonMatchingCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LeoninScimitar()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new FixWhatsBroken()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Leonin Scimitar", "Fix What's Broken");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot be cast when X exceeds the player's life total")
    void cannotPayXLife() {
        harness.setHand(player1, List.of(new FixWhatsBroken()));
        harness.setLife(player1, 1);
        addManaForSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
