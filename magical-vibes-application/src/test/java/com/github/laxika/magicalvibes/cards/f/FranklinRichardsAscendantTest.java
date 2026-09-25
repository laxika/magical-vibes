package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({FranklinRichardsAscendant.class, GrizzlyBears.class, Shock.class})
class FranklinRichardsAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers 6 at the beginning of combat after casting a noncreature spell")
    void discoversAfterCastingNoncreatureSpell() {
        Permanent franklin = harness.addToBattlefieldAndReturn(player1, new FranklinRichardsAscendant());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        castNoncreatureSpell();
        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == discovered);
    }

    @Test
    @DisplayName("May put the discovered card into hand")
    void mayPutDiscoveredCardIntoHand() {
        Permanent franklin = harness.addToBattlefieldAndReturn(player1, new FranklinRichardsAscendant());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));

        castNoncreatureSpell();
        advanceToBeginningOfCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == discovered);
    }

    @Test
    @DisplayName("Does not discover after casting only a creature spell")
    void doesNotDiscoverAfterCastingCreatureSpell() {
        Permanent franklin = harness.addToBattlefieldAndReturn(player1, new FranklinRichardsAscendant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactlyInAnyOrder(franklin, findPermanent(player1, "Grizzly Bears"));
    }

    private void castNoncreatureSpell() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
