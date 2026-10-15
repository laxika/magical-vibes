package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdaptiveGemguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GargantuanLeech;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenCourtyard.class, Forest.class, GrizzlyBears.class,
        AdaptiveGemguard.class, GargantuanLeech.class})
class HiddenCourtyardTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new HiddenCourtyard()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void tapsForWhiteMana() {
        Permanent courtyard = addReadyCourtyard();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(courtyard.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndDiscoversFour() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), discovered));
        Permanent courtyard = addReadyCourtyard();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(courtyard.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    void castsManaValueFourCardFromExileWithoutPayingItsManaCost() {
        Forest skippedLand = new Forest();
        GargantuanLeech skippedExpensiveCard = new GargantuanLeech();
        AdaptiveGemguard discovered = new AdaptiveGemguard();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedExpensiveCard, discovered, untouched));
        Permanent courtyard = addReadyCourtyard();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(courtyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(courtyard.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skippedLand, skippedExpensiveCard);
        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getCard()).isSameAs(discovered);
            assertThat(entry.getSourceZone()).isEqualTo(Zone.EXILE);
        });
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Adaptive Gemguard");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
    }

    @Test
    void returnsAllSkippedCardsWhenNoCardQualifies() {
        Forest land = new Forest();
        GargantuanLeech expensive = new GargantuanLeech();
        harness.setLibrary(player1, List.of(land, expensive));
        addReadyCourtyard();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .doesNotContain(land.getId(), expensive.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land, expensive);
    }

    @Test
    void discoversWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent courtyard = addReadyCourtyard();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(courtyard.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDiscoverDuringCombat() {
        Permanent courtyard = addReadyCourtyard();
        addDiscoverMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(courtyard);
        assertThat(courtyard.isTapped()).isFalse();
    }

    @Test
    void cannotActivateDiscoverOnOpponentsTurn() {
        Permanent courtyard = addReadyCourtyard();
        addDiscoverMana();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(courtyard);
    }

    @Test
    void cannotActivateDiscoverWhileTapped() {
        Permanent courtyard = addReadyCourtyard();
        courtyard.tap();
        addDiscoverMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(courtyard);
    }

    @Test
    void cannotActivateDiscoverWithAnAbilityOnTheStack() {
        addReadyCourtyard();
        Permanent secondCourtyard = addReadyCourtyard();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondCourtyard);
        assertThat(secondCourtyard.isTapped()).isFalse();
    }

    @Test
    void cannotPayTheWhiteCostWithOnlyColorlessMana() {
        Permanent courtyard = addReadyCourtyard();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(courtyard);
        assertThat(courtyard.isTapped()).isFalse();
    }

    private void addDiscoverMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addReadyCourtyard() {
        return addCreatureReady(player1, new HiddenCourtyard());
    }
}
