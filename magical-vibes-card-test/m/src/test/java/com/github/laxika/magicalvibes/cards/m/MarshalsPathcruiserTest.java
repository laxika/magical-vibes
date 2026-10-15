package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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

@CardUsed({MarshalsPathcruiser.class, Plains.class, Forest.class, Island.class,
        BrightfieldGlider.class, EnsoulArtifact.class})
@DisplayName("Marshals' Pathcruiser")
class MarshalsPathcruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield searches for a basic land and puts it into hand")
    void enteringSearchesForBasicLand() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new BrightfieldGlider()));
        harness.castFromHand(player1, new MarshalsPathcruiser(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        Card chosen = search.params().cards().getFirst();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exhaust animates it permanently and adds two +1/+1 counters")
    void exhaustAnimatesPermanentlyAndAddsTwoCounters() {
        Permanent pathcruiser = addReadyPathcruiser();
        addFiveColorMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, pathcruiser)).isTrue();
        assertThat(gqs.isArtifact(pathcruiser)).isTrue();
        assertThat(pathcruiser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, pathcruiser)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, pathcruiser)).isEqualTo(7);
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addReadyPathcruiser();
        addFiveColorMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Crew 5 taps five creatures and animates only until end of turn")
    void crewAnimatesUntilEndOfTurn() {
        Permanent pathcruiser = addReadyPathcruiser();
        List<Permanent> crew = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider()))
                .toList();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(crew).allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, pathcruiser)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pathcruiser)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, pathcruiser)).isEqualTo(5);
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, pathcruiser)).isFalse();
        assertThat(gqs.isArtifact(gd, pathcruiser)).isTrue();
    }

    @Test
    @DisplayName("Crew cannot be paid with only four total power")
    void crewRequiresFivePower() {
        addReadyPathcruiser();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new BrightfieldGlider());
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Exhaust animation and activation restriction persist into later turns")
    void exhaustPersistsAcrossTurns() {
        Permanent pathcruiser = addReadyPathcruiser();
        addFiveColorMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, pathcruiser)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pathcruiser)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, pathcruiser)).isEqualTo(7);
        addFiveColorMana();
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("A basic land search may fail to find even when a basic land is available")
    void searchMayFailToFind() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.castFromHand(player1, new MarshalsPathcruiser(), "{3}");
        resolveAllTriggers();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exhaust does not overwrite an earlier effect setting base power and toughness")
    void exhaustPreservesEarlierBasePowerAndToughness() {
        Permanent pathcruiser = addReadyPathcruiser();
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, pathcruiser.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, pathcruiser)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, pathcruiser)).isEqualTo(5);

        addFiveColorMana();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(pathcruiser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, pathcruiser)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, pathcruiser)).isEqualTo(7);
    }

    private Permanent addReadyPathcruiser() {
        Permanent pathcruiser = harness.addToBattlefieldAndReturn(player1, new MarshalsPathcruiser());
        pathcruiser.setSummoningSick(false);
        return pathcruiser;
    }

    private void addFiveColorMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
