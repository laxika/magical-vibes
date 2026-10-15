package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Banehound;
import com.github.laxika.magicalvibes.cards.c.CharityExtractor;
import com.github.laxika.magicalvibes.cards.p.Prismite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Neoform.class, Banehound.class, Prismite.class, CharityExtractor.class})
class NeoformTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and puts a creature with one higher mana value onto the battlefield with a +1/+1 counter")
    void sacrificesAndSearchesForNextManaValueCreature() {
        Permanent sacrifice = addCreatureReady(player1, new Banehound());
        prepareSpell();
        harness.setLibrary(player1, List.of(new Prismite(), new CharityExtractor(), new Banehound(), new Neoform()));

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Banehound");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Prismite");

        harness.handleCardChosen(player1, 0);

        Permanent found = findPermanent(player1, "Prismite");
        assertThat(found.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(found.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Charity Extractor", "Banehound", "Neoform");
        harness.assertInGraveyard(player1, "Neoform");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayFailToFindEvenWhenAnEligibleCreatureExists() {
        Permanent sacrifice = addCreatureReady(player1, new Banehound());
        prepareSpell();
        harness.setLibrary(player1, List.of(new Prismite()));

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Prismite");
        harness.assertInGraveyard(player1, "Banehound");
        harness.assertInGraveyard(player1, "Neoform");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithoutAnEligibleCreature() {
        Permanent sacrifice = addCreatureReady(player1, new Banehound());
        prepareSpell();
        harness.setLibrary(player1, List.of(new CharityExtractor(), new Neoform()));

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Charity Extractor", "Neoform");
        harness.assertInGraveyard(player1, "Banehound");
        harness.assertInGraveyard(player1, "Neoform");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotCastWithoutSacrificingACreature() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Neoform");
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new Banehound());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Banehound");
        harness.assertInHand(player1, "Neoform");
    }

    @Test
    void sacrificedFaceDownCreatureHasZeroManaValue() {
        Permanent sacrifice = addCreatureReady(player1, new CharityExtractor());
        sacrifice.setFaceDownAsCloaked();
        prepareSpell();
        harness.setLibrary(player1, List.of(new Banehound(), new Prismite()));

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Banehound");
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Banehound").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        harness.assertInGraveyard(player1, "Charity Extractor");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new Neoform()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
