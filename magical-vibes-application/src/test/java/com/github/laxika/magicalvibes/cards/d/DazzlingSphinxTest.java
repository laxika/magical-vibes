package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DazzlingSphinx.class, Forest.class, Divination.class, Brainstorm.class})
class DazzlingSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage reveals until an instant or sorcery and offers it for free")
    void combatDamageOffersFirstInstantOrSorcery() {
        Card forest = new Forest();
        Card divination = new Divination();
        harness.setLibrary(player2, List.of(forest, divination));

        Permanent sphinx = addCreatureReady(player1, new DazzlingSphinx());
        sphinx.setAttacking(true);
        resolveCombat();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).containsExactly(divination);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(divination.getId())
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL
                && entry.getControllerId().equals(player1.getId()));
    }

    @Test
    void cardsAreInExileWhileTheFreeCastDecisionIsPending() {
        Card skipped = new DazzlingSphinx();
        Card instant = new Brainstorm();
        Card untouched = new DazzlingSphinx();
        harness.setLibrary(player2, List.of(skipped, instant, untouched));

        Permanent sphinx = addCreatureReady(player1, new DazzlingSphinx());
        sphinx.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(skipped.getId(), instant.getId());
    }

    @Test
    void instantIsCastFromExileAndSkippedCardsReturnToTheDamagedPlayersLibrary() {
        Card skipped = new DazzlingSphinx();
        Card instant = new Brainstorm();
        Card untouched = new DazzlingSphinx();
        harness.setLibrary(player2, List.of(skipped, instant, untouched));

        Permanent sphinx = addCreatureReady(player1, new DazzlingSphinx());
        sphinx.setAttacking(true);
        resolveCombat();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouched, skipped);
        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getCard().getId()).isEqualTo(instant.getId());
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
            assertThat(entry.getSourceZone()).isEqualTo(Zone.EXILE);
        });
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .doesNotContain(skipped.getId(), instant.getId());
    }

    @Test
    void decliningReturnsAllExiledCardsBelowTheUntouchedLibrary() {
        Card skipped = new DazzlingSphinx();
        Card instant = new Brainstorm();
        Card untouched = new DazzlingSphinx();
        harness.setLibrary(player2, List.of(skipped, instant, untouched));

        Permanent sphinx = addCreatureReady(player1, new DazzlingSphinx());
        sphinx.setAttacking(true);
        resolveCombat();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skipped, instant);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(instant.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void libraryWithoutAnInstantOrSorceryIsReturnedWithoutACastOffer() {
        Card first = new DazzlingSphinx();
        Card second = new DazzlingSphinx();
        harness.setLibrary(player2, List.of(first, second));

        Permanent sphinx = addCreatureReady(player1, new DazzlingSphinx());
        sphinx.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .doesNotContain(first.getId(), second.getId());
    }

    @Test
    void emptyLibraryDoesNotOfferACastOrCauseALoss() {
        harness.setLibrary(player2, List.of());

        Permanent sphinx = addCreatureReady(player1, new DazzlingSphinx());
        sphinx.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
