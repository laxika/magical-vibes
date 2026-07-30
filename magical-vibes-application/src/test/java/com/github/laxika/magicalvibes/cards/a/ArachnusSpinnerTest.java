package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArachnusSpinnerTest extends BaseCardTest {

    private Permanent addSpinner() {
        Permanent spinner = new Permanent(new ArachnusSpinner());
        spinner.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spinner);
        return spinner;
    }

    private UUID addOpposingBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);
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
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new ArachnusWeb());
        deck.add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertWebAttachedTo(bearsId);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Arachnus Web"));
    }

    @Test
    @DisplayName("Another untapped Spider can pay the tap cost instead of the Spinner")
    void anotherSpiderPaysTheTapCost() {
        Permanent spinner = addSpinner();
        Permanent giantSpider = new Permanent(new GiantSpider());
        giantSpider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giantSpider);
        UUID bearsId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.handlePermanentChosen(player1, giantSpider.getId());
        harness.passBothPriorities();

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
        Permanent elves = new Permanent(new LlanowarElves());
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elves);
        UUID bearsId = addOpposingBears();
        harness.setGraveyard(player1, List.of(new ArachnusWeb()));

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elves.isTapped()).isFalse();
    }
}
