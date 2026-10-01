package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.b.BullAurochs;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AurochsHerd.class, BullAurochs.class, BorealDruid.class})
class AurochsHerdTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield may search for an Aurochs card into hand")
    void enteringMaySearchForAurochs() {
        harness.setLibrary(player1, List.of(new BullAurochs(), new BorealDruid()));
        harness.castFromHand(player1, new AurochsHerd(), "{5}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Bull Aurochs");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Bull Aurochs");
    }

    @Test
    @DisplayName("Declining the enters-the-battlefield search does nothing")
    void decliningSearchDoesNothing() {
        List<Card> library = List.of(new BullAurochs(), new BorealDruid());
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new AurochsHerd(), "{5}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Bull Aurochs");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Attacking alone gets no bonus")
    void attackingAloneGetsNoBonus() {
        Permanent herd = addCreatureReady(player1, new AurochsHerd());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(herd.getPowerModifier()).isZero();
        assertThat(herd.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking with another Aurochs gives +1/+0")
    void boostsForEachOtherAttackingAurochs() {
        Permanent herd = addCreatureReady(player1, new AurochsHerd());
        addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(herd.getPowerModifier()).isEqualTo(1);
        assertThat(herd.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +2/+0 when attacking with two other Aurochs")
    void boostsForEachOtherAttackingAurochsTwice() {
        Permanent herd = addCreatureReady(player1, new AurochsHerd());
        addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(herd.getPowerModifier()).isEqualTo(2);
        assertThat(herd.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Other attacking creatures that are not Aurochs do not count")
    void ignoresNonAurochs() {
        Permanent herd = addCreatureReady(player1, new AurochsHerd());
        addCreatureReady(player1, new BorealDruid());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(herd.getPowerModifier()).isZero();
        assertThat(herd.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attack bonus ends at end of turn")
    void boostEndsAtEndOfTurn() {
        Permanent herd = addCreatureReady(player1, new AurochsHerd());
        addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(herd.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(herd.getPowerModifier()).isZero();
        assertThat(herd.getToughnessModifier()).isZero();
    }
}
