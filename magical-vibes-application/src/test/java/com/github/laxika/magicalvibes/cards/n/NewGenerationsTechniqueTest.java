package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InsectoidExterminator;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({NewGenerationsTechnique.class, Forest.class, InsectoidExterminator.class, Plains.class, NorthamptonFarm.class})
class NewGenerationsTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to two basic lands and puts them onto the battlefield tapped")
    void searchesForUpToTwoBasicLands() {
        Card plains = new Plains();
        Card forest = new Forest();
        Card nonLand = new InsectoidExterminator();
        setLibrary(plains, forest, nonLand);
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        addManaForNormalCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(plains, forest);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonLand);
    }

    @Test
    @DisplayName("May find only one basic land")
    void searchesForOnlyOneAvailableBasicLand() {
        Card forest = new Forest();
        setLibrary(forest, new InsectoidExterminator());
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        addManaForNormalCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker")
    void sneakReturnsAnUnblockedAttacker() {
        Permanent attacker = prepareSneak(TurnStep.DECLARE_BLOCKERS);
        setLibrary(new InsectoidExterminator());

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Insectoid Exterminator");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("May find no lands even when two basics are available")
    void mayFindNoLands() {
        Card forest = new Forest();
        Card plains = new Plains();
        setLibrary(forest, plains);
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        addManaForNormalCost();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "New Generation's Technique");
    }

    @Test
    @DisplayName("May stop after finding one land even when another is available")
    void mayStopAfterOneLand() {
        Card forest = new Forest();
        Card plains = new Plains();
        setLibrary(forest, plains);
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        addManaForNormalCost();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May find two basic lands with the same name but excludes nonbasic lands")
    void findsSameNamedBasicsAndExcludesNonbasicLand() {
        Card first = new Forest();
        Card second = new Forest();
        Card nonbasic = new NorthamptonFarm();
        setLibrary(first, second, nonbasic);
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        addManaForNormalCost();

        harness.castAndResolveSorcery(player1, 0, 0);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(first, second);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
    }

    @Test
    @DisplayName("Resolves normally with an empty library")
    void resolvesWithEmptyLibrary() {
        setLibrary();
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        addManaForNormalCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "New Generation's Technique");
    }

    @Test
    @DisplayName("Sneak resolves the same land search and returns the attacker as a cost")
    void sneakSearchesForLands() {
        Permanent attacker = prepareSneak(TurnStep.DECLARE_BLOCKERS);
        Card forest = new Forest();
        setLibrary(forest);

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));

        harness.assertInHand(player1, "Insectoid Exterminator");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        harness.assertInGraveyard(player1, "New Generation's Technique");
    }

    @Test
    @DisplayName("Sneak cannot return a blocked attacker even after its blockers are gone")
    void sneakRejectsBlockedAttacker() {
        Permanent attacker = prepareSneak(TurnStep.DECLARE_BLOCKERS);
        attacker.setBlockedWithoutBlockers(true);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInHand(player1, "New Generation's Technique");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sneak cannot be used before blockers are declared")
    void sneakRejectsDeclareAttackersStep() {
        Permanent attacker = prepareSneak(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Normal mana cost does not grant permission to cast during declare blockers")
    void normalCostRejectsCombatTiming() {
        prepareSneak(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "New Generation's Technique");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent prepareSneak(TurnStep step) {
        Permanent attacker = addCreatureReady(player1, new InsectoidExterminator());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new NewGenerationsTechnique()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.clearPriorityPassed();
        return attacker;
    }

    private void addManaForNormalCost() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
