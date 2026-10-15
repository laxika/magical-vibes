package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampantRejuvenator.class, FlameJavelin.class, Forest.class, Plains.class})
class RampantRejuvenatorTest extends BaseCardTest {

    @Test
    @DisplayName("Rampant Rejuvenator enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent rejuvenator = harness.enterBattlefieldAndReturn(player1, new RampantRejuvenator());

        assertThat(rejuvenator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(rejuvenator.getEffectivePower()).isEqualTo(2);
        assertThat(rejuvenator.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Rampant Rejuvenator dies, it searches for basic lands equal to its power")
    void deathSearchUsesPower() {
        Permanent rejuvenator = harness.enterBattlefieldAndReturn(player1, new RampantRejuvenator());
        rejuvenator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Card forest = new Forest();
        Card plains = new Plains();
        Card extraForest = new Forest();
        Card extraPlains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains, extraForest, extraPlains));

        killWithFlameJavelin(player2, rejuvenator);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().cards()).containsExactly(forest, plains, extraForest, extraPlains);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void killWithFlameJavelin(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new FlameJavelin()));
        harness.addMana(caster, ManaColor.RED, 6);

        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The death search can stop after finding fewer lands than its power")
    void canChooseFewerLands() {
        Permanent rejuvenator = harness.enterBattlefieldAndReturn(player1, new RampantRejuvenator());
        Card forest = new Forest();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));

        killWithFlameJavelin(player2, rejuvenator);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The death search can find zero lands even when basic lands are available")
    void canChooseZeroLands() {
        Permanent rejuvenator = harness.enterBattlefieldAndReturn(player1, new RampantRejuvenator());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        killWithFlameJavelin(player2, rejuvenator);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("The death search excludes nonland cards and finishes when fewer basics exist")
    void excludesNonlandsAndFindsOnlyAvailableBasics() {
        Permanent rejuvenator = harness.enterBattlefieldAndReturn(player1, new RampantRejuvenator());
        Card forest = new Forest();
        Card nonland = new RampantRejuvenator();
        harness.setLibrary(player1, List.of(nonland, forest));

        killWithFlameJavelin(player2, rejuvenator);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("A death at zero power finds no lands")
    void zeroPowerFindsNoLands() {
        Permanent rejuvenator = harness.enterBattlefieldAndReturn(player1, new RampantRejuvenator());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        rejuvenator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rampant Rejuvenator");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("The death search finishes with an empty library")
    void emptyLibraryFinishesSearch() {
        Permanent rejuvenator = harness.enterBattlefieldAndReturn(player1, new RampantRejuvenator());
        harness.setLibrary(player1, List.of());

        killWithFlameJavelin(player2, rejuvenator);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The death search finishes when the library contains no basic lands")
    void noMatchingCardsFinishesSearch() {
        Permanent rejuvenator = harness.enterBattlefieldAndReturn(player1, new RampantRejuvenator());
        Card nonland = new RampantRejuvenator();
        harness.setLibrary(player1, List.of(nonland));

        killWithFlameJavelin(player2, rejuvenator);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }
}
