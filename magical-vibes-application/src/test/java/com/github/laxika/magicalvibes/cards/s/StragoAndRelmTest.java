package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disallow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Harmonize;
import com.github.laxika.magicalvibes.cards.i.IncubationDruid;
import com.github.laxika.magicalvibes.cards.i.InspiringCall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StragoAndRelm.class, Forest.class, GrizzlyBears.class, IncubationDruid.class,
        Harmonize.class, InspiringCall.class, Disallow.class})
class StragoAndRelmTest extends BaseCardTest {

    @Test
    @DisplayName("Finds the first instant, sorcery, or creature in an opponent's library")
    void findsFirstMatchingCard() {
        activateWithLibrary(List.of(new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature cast this way gains haste and is sacrificed at the next end step")
    void creatureGainsHasteAndIsSacrificedAtEndStep() {
        activateWithLibrary(List.of(new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void skippedCardsAndFoundCardAreExiledDuringTheCastDecision() {
        Forest skipped = new Forest();
        IncubationDruid hit = new IncubationDruid();
        Forest remaining = new Forest();
        activateWithLibrary(List.of(skipped, hit, remaining));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.findExiledCard(skipped.getId())).isNotNull();
        assertThat(gd.findExiledCard(hit.getId())).isNotNull();
    }

    @Test
    void decliningToCastLeavesAllRevealedCardsInExile() {
        Forest skipped = new Forest();
        IncubationDruid hit = new IncubationDruid();
        activateWithLibrary(List.of(skipped, hit));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(skipped.getId())).isNotNull();
        assertThat(gd.findExiledCard(hit.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Incubation Druid");
    }

    @Test
    void libraryWithNoMatchingCardIsEntirelyExiled() {
        Forest first = new Forest();
        Forest second = new Forest();
        activateWithLibrary(List.of(first, second));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void castingTheFoundCreatureLeavesSkippedCardsInExile() {
        Forest skipped = new Forest();
        IncubationDruid hit = new IncubationDruid();
        activateWithLibrary(List.of(skipped, hit));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Incubation Druid");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(skipped.getId())).isNotNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
    }

    @Test
    void castsSorceryWithoutPayingItsManaCost() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));
        activateWithLibrary(List.of(new Harmonize()));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        harness.assertInGraveyard(player2, "Harmonize");
    }

    @Test
    void castsInstantWithoutPayingItsManaCost() {
        activateWithLibrary(List.of(new InspiringCall()));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Inspiring Call");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotOpenACastDecision() {
        activateWithLibrary(List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotTargetItsController() {
        activateWithLibrary(List.of(new IncubationDruid()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        findPermanent(player1, "Strago and Relm").untap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateOutsideAMainPhase() {
        activateWithLibrary(List.of(new IncubationDruid()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        findPermanent(player1, "Strago and Relm").untap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeAbilityTriggersAgainAfterItsFirstTriggerIsCountered() {
        activateWithLibrary(List.of(new IncubationDruid()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent creature = findPermanent(player1, "Incubation Druid");
        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.castAndResolveInstant(player1, 0, creature.getCard().getId());
        harness.assertOnBattlefield(player1, "Incubation Druid");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Incubation Druid");
        harness.assertInGraveyard(player2, "Incubation Druid");
    }

    private void activateWithLibrary(List<Card> library) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player2, library);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new StragoAndRelm());
        source.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
    }
}
