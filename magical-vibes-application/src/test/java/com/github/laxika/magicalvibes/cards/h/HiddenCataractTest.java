package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdaptiveGemguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaraudingBrinefang;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenCataract.class, Forest.class, GrizzlyBears.class, AdaptiveGemguard.class, MaraudingBrinefang.class})
class HiddenCataractTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new HiddenCataract()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void tapsForBlueMana() {
        Permanent cataract = addReadyCataract();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(cataract.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndDiscoversFour() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), discovered));
        addReadyCataract();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Hidden Cataract");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void paysSacrificeAndManaBeforeDiscoverResolves() {
        addReadyCataract();
        harness.setLibrary(player1, List.of(new AdaptiveGemguard()));
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Hidden Cataract");
        harness.assertInGraveyard(player1, "Hidden Cataract");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void castsManaValueFourCardForFreeAndBottomsSkippedCards() {
        Forest skippedLand = new Forest();
        MaraudingBrinefang skippedExpensiveCard = new MaraudingBrinefang();
        AdaptiveGemguard discovered = new AdaptiveGemguard();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedExpensiveCard, discovered, untouched));
        addReadyCataract();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skippedLand, skippedExpensiveCard);
        assertThat(gd.exiledCards).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Adaptive Gemguard");
        harness.assertInGraveyard(player1, "Hidden Cataract");
    }

    @Test
    void returnsEntireLibraryWhenNoCardQualifies() {
        Forest land = new Forest();
        MaraudingBrinefang expensiveCard = new MaraudingBrinefang();
        harness.setLibrary(player1, List.of(land, expensiveCard));
        addReadyCataract();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensiveCard);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Hidden Cataract");
    }

    @Test
    void discoversFromEmptyLibraryWithoutRequestingChoice() {
        harness.setLibrary(player1, List.of());
        addReadyCataract();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Hidden Cataract");
    }

    @Test
    void cannotDiscoverOutsideMainPhase() {
        Permanent cataract = addReadyCataract();
        addDiscoverMana();
        gd.currentStep = TurnStep.BEGINNING_OF_COMBAT;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cataract.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Hidden Cataract");
        harness.assertNotInGraveyard(player1, "Hidden Cataract");
    }

    @Test
    void cannotUseManaAbilityAndThenSacrificeTheTappedLand() {
        Permanent cataract = addReadyCataract();
        addDiscoverMana();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cataract.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Hidden Cataract");
        harness.assertNotInGraveyard(player1, "Hidden Cataract");
    }

    @Test
    void cannotDiscoverWithoutTheBlueManaCost() {
        Permanent cataract = addReadyCataract();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(cataract.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Hidden Cataract");
        harness.assertNotInGraveyard(player1, "Hidden Cataract");
    }

    @Test
    void cannotDiscoverOnOpponentsTurn() {
        harness.addToBattlefield(player2, new HiddenCataract());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Hidden Cataract");
        harness.assertNotInGraveyard(player2, "Hidden Cataract");
    }

    @Test
    void cannotDiscoverWithAnAbilityOnTheStack() {
        addReadyCataract();
        addReadyCataract();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    private void addDiscoverMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addReadyCataract() {
        return addCreatureReady(player1, new HiddenCataract());
    }
}
