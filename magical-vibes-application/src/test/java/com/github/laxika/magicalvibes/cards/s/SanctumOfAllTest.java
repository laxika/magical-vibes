package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HondenOfCleansingFire;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumOfAll.class, Forest.class, HondenOfCleansingFire.class,
        HondenOfSeeingWinds.class, SanctumOfFruitfulHarvest.class,
        SanctumOfTranquilLight.class, SanctumOfCalmWaters.class,
        SanctumOfStoneFangs.class, SanctumOfShatteredHeights.class})
class SanctumOfAllTest extends BaseCardTest {

    @Test
    @DisplayName("May put a Shrine from the library onto the battlefield")
    void searchesLibraryForShrine() {
        harness.setLibrary(player1, List.of(new Forest(),
                new HondenOfSeeingWinds()));
        harness.addToBattlefield(player1, new SanctumOfAll());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        HondenOfSeeingWinds honden = gd.playerDecks.get(player1.getId()).stream()
                .filter(HondenOfSeeingWinds.class::isInstance)
                .map(HondenOfSeeingWinds.class::cast)
                .findFirst()
                .orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(honden.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HondenOfSeeingWinds);
    }

    @Test
    @DisplayName("May put a Shrine from the graveyard onto the battlefield")
    void searchesGraveyardForShrine() {
        HondenOfSeeingWinds honden = new HondenOfSeeingWinds();
        harness.setGraveyard(player1, List.of(honden));
        harness.addToBattlefield(player1, new SanctumOfAll());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(honden.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == honden);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(honden);
    }

    @Test
    @DisplayName("Doubles another Shrine's upkeep trigger at six Shrines")
    void doublesAnotherShrinesUpkeepTriggerAtSixShrines() {
        harness.addToBattlefield(player1, new SanctumOfAll());
        harness.addToBattlefield(player1, new HondenOfCleansingFire());
        addGenericShrines(4);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(44);
    }

    @Test
    @DisplayName("Doubles another Shrine's trigger at six Shrines")
    void doublesAnotherShrinesTriggerAtSixShrines() {
        harness.addToBattlefield(player1, new SanctumOfAll());
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
        addGenericShrines(4);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(12);
    }

    @Test
    @DisplayName("Does not double another Shrine's trigger below six Shrines")
    void doesNotDoubleAnotherShrinesTriggerBelowSixShrines() {
        harness.addToBattlefield(player1, new SanctumOfAll());
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
        addGenericShrines(3);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(5);
    }

    private void addGenericShrines(int count) {
        for (int i = 0; i < count; i++) {
            Card shrine = new Card();
            shrine.setName("Test Shrine " + i);
            shrine.setType(CardType.ENCHANTMENT);
            shrine.setSubtypes(List.of(CardSubtype.SHRINE));
            harness.addToBattlefield(player1, shrine);
        }
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    void mayDeclineSearch() {
        SanctumOfTranquilLight shrine = new SanctumOfTranquilLight();
        harness.setLibrary(player1, List.of(shrine));
        harness.addToBattlefield(player1, new SanctumOfAll());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shrine);
        harness.assertNotOnBattlefield(player1, "Sanctum of Tranquil Light");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotDoubleItsOwnUpkeepTriggerAtSixShrines() {
        addSixM21Shrines();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void shrineEnteringDuringUpkeepDoesNotTriggerThatUpkeep() {
        HondenOfCleansingFire shrine = new HondenOfCleansingFire();
        harness.setGraveyard(player1, List.of(shrine));
        harness.addToBattlefield(player1, new SanctumOfAll());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(shrine.getId()));

        harness.assertOnBattlefield(player1, "Honden of Cleansing Fire");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDoubleOpponentsShrineTrigger() {
        addSixM21Shrines();
        harness.addToBattlefield(player2, new HondenOfCleansingFire());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        assertThat(gd.stack).isEmpty();
    }

    private void addSixM21Shrines() {
        harness.addToBattlefield(player1, new SanctumOfAll());
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.addToBattlefield(player1, new SanctumOfCalmWaters());
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());
        harness.addToBattlefield(player1, new SanctumOfShatteredHeights());
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
    }
}
