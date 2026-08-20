package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AurochsHerdTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield may search for an Aurochs card into hand")
    void enteringMaySearchForAurochs() {
        harness.setLibrary(player1, List.of(new Aurochs(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new AurochsHerd()));
        addHerdMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Aurochs");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Aurochs");
    }

    @Test
    @DisplayName("Attacking with another Aurochs gives +1/+0")
    void boostsForEachOtherAttackingAurochs() {
        Permanent herd = addCreatureReady(new AurochsHerd());
        addCreatureReady(new Aurochs());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(herd.getPowerModifier()).isEqualTo(1);
        assertThat(herd.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addCreatureReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addHerdMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
