package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkWithTheAncestors.class, Forest.class, GrizzlyBears.class, HolyDay.class})
class WalkWithTheAncestorsTest extends BaseCardTest {

    @Test
    void returnsAPermanentCardAndDiscoversFour() {
        Card returned = new GrizzlyBears();
        Card discovered = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(returned));
        harness.setLibrary(player1, List.of(land, discovered));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void canDiscoverWithoutReturningACard() {
        Card discovered = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    void cannotTargetANonpermanentCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
