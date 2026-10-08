package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AdaptiveGemguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.cards.t.TectonicHazard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkWithTheAncestors.class, Forest.class, AdaptiveGemguard.class, PanickedAltisaur.class, TectonicHazard.class})
class WalkWithTheAncestorsTest extends BaseCardTest {

    @Test
    void returnsAPermanentCardAndDiscoversFour() {
        Card returned = new AdaptiveGemguard();
        Card discovered = new AdaptiveGemguard();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(returned));
        harness.setLibrary(player1, List.of(land, discovered));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, returned.getId());

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
        Card discovered = new AdaptiveGemguard();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    void cannotTargetANonpermanentCard() {
        Card sorcery = new TectonicHazard();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canReturnALandCard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void cannotTargetAPermanentInOpponentsGraveyard() {
        Card permanent = new AdaptiveGemguard();
        harness.setGraveyard(player2, List.of(permanent));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDeclineAnAvailableGraveyardTargetAndCastTheDiscoveredCardForFree() {
        Card permanent = new AdaptiveGemguard();
        Card discovered = new AdaptiveGemguard();
        harness.setGraveyard(player1, List.of(permanent));
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(permanent);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(permanent, discovered);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> assertThat(p.getCard()).isSameAs(discovered));
    }

    @Test
    void skipsLandsAndCardsAboveFourAndPutsThemBelowTheUntouchedLibrary() {
        Card land = new Forest();
        Card expensive = new PanickedAltisaur();
        Card discovered = new AdaptiveGemguard();
        Card untouched = new TectonicHazard();
        harness.setLibrary(player1, List.of(land, expensive, discovered, untouched));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discovered);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, expensive);
    }

    @Test
    void doesNotDiscoverWhenItsOnlyTargetLeavesTheGraveyard() {
        Card target = new AdaptiveGemguard();
        Card discovered = new AdaptiveGemguard();
        Card spell = new WalkWithTheAncestors();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(discovered);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void finishesDiscoverWhenThereIsNoQualifyingCard() {
        Card land = new Forest();
        Card expensive = new PanickedAltisaur();
        harness.setLibrary(player1, List.of(land, expensive));
        harness.setHand(player1, List.of(new WalkWithTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
