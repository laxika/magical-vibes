package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BywayCourier;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrawlingSensation.class, Forest.class, BywayCourier.class, CrowOfDarkTidings.class})
class CrawlingSensationTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger mills two cards")
    void upkeepMillsTwoCards() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new BywayCourier(), new BywayCourier(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not mill")
    void decliningUpkeepDoesNotMill() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new BywayCourier(), new BywayCourier()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling multiple lands creates only one Insect token per turn")
    void millingMultipleLandsCreatesOneInsectTokenPerTurn() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new BywayCourier()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(countInsectTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("A land milled before the enchantment enters consumes the first event of the turn")
    void earlierLandBeforeEnteringPreventsToken() {
        harness.setLibrary(player1, List.of(new Forest(), new BywayCourier(),
                new Forest(), new BywayCourier()));
        harness.enterBattlefieldAndReturn(player1, new CrowOfDarkTidings());
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        harness.enterBattlefieldAndReturn(player1, new CrawlingSensation());
        harness.enterBattlefieldAndReturn(player1, new CrowOfDarkTidings());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(countInsectTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Separate land events in one turn create only one token")
    void separateLandEventsCreateOnlyOneToken() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new CrowOfDarkTidings());
        resolveAllTriggers();
        assertThat(countInsectTokens(player1)).isEqualTo(1);
        harness.enterBattlefieldAndReturn(player1, new CrowOfDarkTidings());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(countInsectTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first land event on an opponent's turn can create another token")
    void triggerResetsOnOpponentsTurn() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.enterBattlefieldAndReturn(player1, new CrowOfDarkTidings());
        resolveAllTriggers();
        assertThat(countInsectTokens(player1)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new CrowOfDarkTidings());
        resolveAllTriggers();

        assertThat(countInsectTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land cards do not create a token for the controller")
    void opponentsLandsDoNotTrigger() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player2, new CrowOfDarkTidings());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(countInsectTokens(player1)).isZero();
    }

    @Test
    @DisplayName("A library with only one land mills that card and creates a token")
    void upkeepWithOneCardMillsAvailableCard() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(countInsectTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each copy creates its own token for the first land event")
    void multipleCopiesEachCreateToken() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new CrowOfDarkTidings());
        resolveAllTriggers();

        assertThat(countInsectTokens(player1)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .allSatisfy(permanent -> {
                    assertThat(permanent.getEffectivePower()).isEqualTo(1);
                    assertThat(permanent.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Accepting the upkeep trigger with an empty library creates no token")
    void emptyLibraryCreatesNoToken() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(countInsectTokens(player1)).isZero();
    }

    private long countInsectTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.INSECT))
                .count();
    }
}
