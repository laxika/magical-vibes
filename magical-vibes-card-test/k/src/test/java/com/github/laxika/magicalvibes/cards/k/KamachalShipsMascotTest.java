package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamachalShipsMascot.class, Forest.class, GrizzlyBears.class})
class KamachalShipsMascotTest extends BaseCardTest {

    @Test
    void redAbilityBoostsPowerUntilEndOfTurn() {
        Permanent kamachal = addCreatureReady(player1, new KamachalShipsMascot());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kamachal.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kamachal.getPowerModifier()).isZero();
    }

    @Test
    void combatDamageCreatesTreasureAndExilesExactManaValueCardForCasting() {
        addCreatureReady(player1, new KamachalShipsMascot());
        CardInLibrary library = setDamageTwoLibrary();

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        ExiledCardEntry exiled = gd.findExiledCard(library.matchingCard().getId());
        assertThat(exiled).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.nonMatchingCard());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, library.matchingCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(library.matchingCard().getId())).isNull();
    }

    @Test
    void noMatchingManaValueLeavesLibraryUntouchedAfterCreatingTreasure() {
        addCreatureReady(player1, new KamachalShipsMascot());
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void boostedCombatDamageExilesManaValueThreeRatherThanPrintedPower() {
        addCreatureReady(player1, new KamachalShipsMascot());
        KamachalShipsMascot matching = new KamachalShipsMascot();
        GrizzlyBears nonMatching = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonMatching, matching));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.findExiledCard(matching.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonMatching);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void multipleMatchingCardsExileExactlyOneAndPreserveOtherLibraryCards() {
        addCreatureReady(player1, new KamachalShipsMascot());
        Forest forest = new Forest();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, forest, second));

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        boolean firstExiled = gd.findExiledCard(first.getId()) != null;
        boolean secondExiled = gd.findExiledCard(second.getId()) != null;
        assertThat(firstExiled ^ secondExiled).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(
                firstExiled ? List.of(forest, second) : List.of(first, forest));
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void emptyLibraryStillCreatesTreasure() {
        addCreatureReady(player1, new KamachalShipsMascot());
        harness.setLibrary(player2, List.of());

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exiledCardRequiresItsNormalManaCost() {
        addCreatureReady(player1, new KamachalShipsMascot());
        CardInLibrary library = setDamageTwoLibrary();
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, library.matchingCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.findExiledCard(library.matchingCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void uncastCardRemainsExiledButPermissionExpiresAfterTheTurn() {
        addCreatureReady(player1, new KamachalShipsMascot());
        CardInLibrary library = setDamageTwoLibrary();
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThat(gd.findExiledCard(library.matchingCard().getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, library.matchingCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void castingPermissionDoesNotOverrideCreatureTiming() {
        addCreatureReady(player1, new KamachalShipsMascot());
        CardInLibrary library = setDamageTwoLibrary();
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThat(gd.findExiledCard(library.matchingCard().getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, library.matchingCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot cast sorcery-speed spell from exile now");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private CardInLibrary setDamageTwoLibrary() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(forest, bears));
        return new CardInLibrary(forest, bears);
    }

    private record CardInLibrary(Forest nonMatchingCard, GrizzlyBears matchingCard) {
    }
}
