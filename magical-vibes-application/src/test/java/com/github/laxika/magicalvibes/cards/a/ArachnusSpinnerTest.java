package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArachnusSpinner.class, ArachnusWeb.class, GiantSpider.class, RuneclawBear.class, LlanowarElves.class})
class ArachnusSpinnerTest extends BaseCardTest {

    private Permanent addSpinner() {
        Permanent spinner = harness.addToBattlefieldAndReturn(player1, new ArachnusSpinner());
        spinner.setSummoningSick(false);
        return spinner;
    }

    private UUID addOpposingBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bears.setSummoningSick(false);
        return bears.getId();
    }

    private void assertWebAttachedTo(UUID hostId) {
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Arachnus Web")
                        && p.isAttached()
                        && hostId.equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Finds Arachnus Web in the graveyard and attaches it to the target creature")
    void findsWebInGraveyard() {
        Permanent spinner = addSpinner();
        UUID bearsId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your graveyard");

        assertWebAttachedTo(bearsId);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Arachnus Web"));
        assertThat(spinner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Searches the library when the graveyard has no Web, attaching the chosen card")
    void findsWebInLibrary() {
        addSpinner();
        UUID bearsId = addOpposingBears();
        harness.setLibrary(player1, List.of(new ArachnusWeb(), new RuneclawBear()));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your library");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1);

        harness.handleCardChosen(player1, 0);

        assertWebAttachedTo(bearsId);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Arachnus Web"));
    }

    @Test
    @DisplayName("Another untapped Spider can pay the tap cost instead of the Spinner")
    void anotherSpiderPaysTheTapCost() {
        Permanent spinner = addSpinner();
        Permanent giantSpider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        giantSpider.setSummoningSick(false);
        UUID bearsId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.handlePermanentChosen(player1, giantSpider.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your graveyard");

        assertWebAttachedTo(bearsId);
        assertThat(giantSpider.isTapped()).isTrue();
        assertThat(spinner.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Fizzles without searching when the target creature is gone on resolution")
    void fizzlesWhenTargetIsGone() {
        addSpinner();
        UUID elvesId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        harness.activateAbility(player1, 0, null, elvesId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Arachnus Web"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Arachnus Web"));
    }

    @Test
    @DisplayName("Non-Spider creatures cannot pay the tap cost")
    void nonSpiderCannotPayTapCost() {
        Permanent spinner = addSpinner();
        spinner.tap();
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(false);
        UUID bearsId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elves.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Spinner can tap itself to pay its ability's cost")
    void summoningSickSpinnerCanPayCost() {
        Permanent spinner = addSpinner();
        spinner.setSummoningSick(true);
        UUID hostId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        harness.activateAbility(player1, 0, null, hostId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your graveyard");

        assertWebAttachedTo(hostId);
        assertThat(spinner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Spinner can activate by tapping a summoning-sick Spider")
    void tappedSpinnerCanUseSummoningSickSpider() {
        Permanent spinner = addSpinner();
        spinner.tap();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        spider.setSummoningSick(true);
        UUID hostId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        harness.activateAbility(player1, 0, null, hostId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your graveyard");

        assertWebAttachedTo(hostId);
        assertThat(spider.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Searching an empty graveyard and library finds no Web")
    void noWebInEitherZone() {
        Permanent spinner = addSpinner();
        UUID hostId = addOpposingBears();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, hostId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your graveyard and library");

        assertThat(spinner.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof ArachnusWeb);
    }

    @Test
    @DisplayName("A Web in hand cannot be found by the ability")
    void doesNotSearchHand() {
        addSpinner();
        UUID hostId = addOpposingBears();
        ArachnusWeb web = new ArachnusWeb();
        harness.setHand(player1, List.of(web));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, hostId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your graveyard and library");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(web);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof ArachnusWeb);
    }

    @Test
    @DisplayName("A Web in the graveyard must not be moved before the controller chooses where to search")
    void graveyardWebDoesNotForceGraveyardSearch() {
        addSpinner();
        UUID hostId = addOpposingBears();
        ArachnusWeb graveyardWeb = new ArachnusWeb();
        harness.setGraveyard(player1, List.of(graveyardWeb));
        harness.setLibrary(player1, List.of(new ArachnusWeb()));

        harness.activateAbility(player1, 0, null, hostId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardWeb);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof ArachnusWeb);
        harness.handleListChoice(player1, "Search your library");
        harness.handleCardChosen(player1, 0);
        assertWebAttachedTo(hostId);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardWeb);
    }

    @Test
    @DisplayName("Searching both zones allows choosing the graveyard Web while leaving the library Web")
    void bothZonesOfferGraveyardAndLibraryMatches() {
        addSpinner();
        UUID hostId = addOpposingBears();
        ArachnusWeb graveyardWeb = new ArachnusWeb();
        ArachnusWeb libraryWeb = new ArachnusWeb();
        harness.setGraveyard(player1, List.of(graveyardWeb));
        harness.setLibrary(player1, List.of(libraryWeb));

        harness.activateAbility(player1, 0, null, hostId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your graveyard and library");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(graveyardWeb, libraryWeb);
        harness.handleCardChosen(player1, search.params().cards().indexOf(graveyardWeb));
        assertWebAttachedTo(hostId);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryWeb);
    }

    @Test
    @DisplayName("A library search may fail to find an available Web")
    void canFailToFindWebInLibrary() {
        addSpinner();
        UUID hostId = addOpposingBears();
        ArachnusWeb web = new ArachnusWeb();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(web));

        harness.activateAbility(player1, 0, null, hostId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your library");
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(web);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof ArachnusWeb);
    }

    @Test
    @DisplayName("An opponent's untapped Spider cannot pay the cost")
    void cannotTapOpponentsSpider() {
        Permanent spinner = addSpinner();
        spinner.tap();
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        UUID hostId = addOpposingBears();

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hostId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(spider.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still resolves after the Spinner leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent spinner = addSpinner();
        UUID hostId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        harness.activateAbility(player1, 0, null, hostId);
        gd.playerBattlefields.get(player1.getId()).remove(spinner);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Search your graveyard");

        assertWebAttachedTo(hostId);
    }
}
