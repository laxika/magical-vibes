package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThawingGlaciers.class, Forest.class, Island.class, GrizzlyBears.class})
class ThawingGlaciersTest extends BaseCardTest {

    @Test
    @DisplayName("It enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ThawingGlaciers()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Thawing Glaciers"))
                .singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    @DisplayName("Activating offers only basic lands, destination battlefield tapped")
    void activationSearchesForBasicLand() {
        activate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped")
    void chosenBasicLandEntersTapped() {
        activate();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND) && !p.getCard().getName().equals("Thawing Glaciers"))
                .singleElement()
                .matches(Permanent::isTapped);
    }

    @Test
    @DisplayName("It returns to its owner's hand at the beginning of the next cleanup step")
    void returnsToHandAtCleanup() {
        activate();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Thawing Glaciers");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thawing Glaciers"));
        harness.assertInHand(player1, "Thawing Glaciers");
    }

    @Test
    @DisplayName("Without activating it stays on the battlefield through cleanup")
    void staysWithoutActivation() {
        harness.addToBattlefield(player1, new ThawingGlaciers());

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Thawing Glaciers");
    }

    @Test
    void mayDeclineToFindBasicLand() {
        activate();
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ThawingGlaciers)
                .noneMatch(p -> p.getCard() instanceof Forest)
                .noneMatch(p -> p.getCard() instanceof Island);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c instanceof ThawingGlaciers);
    }

    @Test
    @CardUsed(PsychogenicProbe.class)
    @DisplayName("Its search still shuffles an empty library")
    void emptyLibraryIsStillShuffled() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.addToBattlefield(player1, new ThawingGlaciers());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private void activate() {
        harness.addToBattlefield(player1, new ThawingGlaciers());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));
    }
}
