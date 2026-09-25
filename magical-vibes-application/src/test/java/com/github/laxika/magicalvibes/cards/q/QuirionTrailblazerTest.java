package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuirionTrailblazer.class, Plains.class, Forest.class, QuirionSentinel.class})
class QuirionTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search for a basic land and put it onto the battlefield tapped")
    void etbSearchesForBasicLand() {
        harness.setHand(player1, List.of(new QuirionTrailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        setupLibrary();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("ETB search can find no basic land")
    void etbSearchCanFindNoBasicLand() {
        harness.setHand(player1, List.of(new QuirionTrailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new QuirionSentinel(), new QuirionSentinel()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("ETB may be declined")
    void etbMayBeDeclined() {
        harness.setHand(player1, List.of(new QuirionTrailblazer()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        setupLibrary();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new QuirionSentinel()));
    }
}
