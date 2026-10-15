package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClarionUltimatum.class, Forest.class, Island.class, DruidOfTheAnima.class, CosisTrickster.class, Clone.class})
class ClarionUltimatumTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Clarion Ultimatum offers all controlled permanents when fewer than five exist")
    void promptsPermanentChoice() {
        List<Permanent> forests = setupForests(3);
        setupLibrary();
        castClarion();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrderElementsOf(forests.stream().map(Permanent::getId).toList());
    }

    @Test
    @DisplayName("The choice is capped at five permanents even with more on the battlefield")
    void choiceCappedAtFive() {
        setupForests(6);
        setupLibrary();
        castClarion();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(5);
    }

    @Test
    @DisplayName("A same-named card can be found for each chosen permanent and enters tapped")
    void fetchesSameNamedCardsTapped() {
        List<Permanent> forests = setupForests(2);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1,
                List.of(forests.get(0).getId(), forests.get(1).getId()));

        // The first same-name pick offers only Forest cards.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> "Forest".equals(c.getName()));

        harness.handleCardChosen(player1, 0);
        // The second Forest allows another same-name pick.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Two original Forests plus two fetched Forests.
        assertThat(forestsOnBattlefield()).hasSize(4);
        long tapped = forestsOnBattlefield().stream().filter(Permanent::isTapped).count();
        assertThat(tapped).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("The search for each chosen permanent is optional")
    void mayDeclineEachSearch() {
        List<Permanent> forests = setupForests(1);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(forests.get(0).getId()));

        // Decline the only same-name search.
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Nothing fetched — only the original Forest remains.
        assertThat(forestsOnBattlefield()).hasSize(1);
    }

    @Test
    @DisplayName("The controller must choose every permanent when fewer than five exist")
    void cannotChooseNoneWhenPermanentsExist() {
        setupForests(3);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(forestsOnBattlefield()).hasSize(3);
    }

    @Test
    @DisplayName("Five permanents must be selected when at least five are controlled")
    void cannotChooseOnlyFourOfSixPermanents() {
        List<Permanent> forests = setupForests(6);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                forests.subList(0, 4).stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("Found cards remain off the battlefield until all same-name choices are complete")
    void foundCardsEnterTogetherAfterAllChoices() {
        List<Permanent> forests = setupForests(2);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, forests.stream().map(Permanent::getId).toList());

        harness.handleCardChosen(player1, 0);

        assertThat(forestsOnBattlefield()).hasSize(2);
        harness.handleCardChosen(player1, 0);
        assertThat(forestsOnBattlefield()).hasSize(4);
        assertThat(forestsOnBattlefield().stream()
                .filter(p -> !forests.contains(p)).toList()).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("A missing same-name card does not prevent finding a card for another permanent")
    void skipsMissingNameAndFindsOtherName() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new DruidOfTheAnima()));
        castClarion();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(island.getId(), forest.getId()));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(forestsOnBattlefield()).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The library is shuffled once after all same-name picks, never between picks")
    void shufflesOnlyAfterAllPicks() {
        List<Permanent> forests = setupForests(2);
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        setupLibrary();
        castClarion();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, forests.stream().map(Permanent::getId).toList());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.handleCardChosen(player1, 0);
            assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gd.stack).isEmpty();
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
        });
        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining one same-name pick does not prevent finding a card for the next permanent")
    void mayDeclineOnePickAndAcceptAnother() {
        List<Permanent> forests = setupForests(2);
        setupLibrary();
        castClarion();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, forests.stream().map(Permanent::getId).toList());

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(forestsOnBattlefield()).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The library is still shuffled when no permanents are controlled")
    void shufflesWithNoControlledPermanents() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        setupLibrary();
        castClarion();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);
        });

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    void foundCloneChoosesItsCopyBeforeTheCardsEnterTogetherTapped() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DruidOfTheAnima());
        Permanent originalClone = harness.addToBattlefieldAndReturn(player1, new Clone());
        originalClone.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new DruidOfTheAnima(), new Clone()));
        castClarion();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(druid.getId(), originalClone.getId()));
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(druid, originalClone);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, druid.getId());

        List<Permanent> entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != druid && permanent != originalClone).toList();
        assertThat(entering).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(entering).allMatch(permanent -> "Druid of the Anima".equals(permanent.getCard().getName()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private List<Permanent> setupForests(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new Forest()))
                .toList();
    }

    private void setupLibrary() {
        harness.setLibrary(player1,
                List.of(new Forest(), new Forest(), new Island(), new DruidOfTheAnima()));
    }

    private void castClarion() {
        harness.setHand(player1, List.of(new ClarionUltimatum()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private List<Permanent> forestsOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Forest".equals(p.getCard().getName()))
                .toList();
    }
}
