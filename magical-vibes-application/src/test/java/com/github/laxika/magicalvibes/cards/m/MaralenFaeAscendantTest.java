package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaralenFaeAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Maralen and another Elf entering exile two cards from a target opponent's library")
    void qualifyingElfEntryExilesTwoCards() {
        Permanent maralen = harness.addToBattlefieldAndReturn(player1, new MaralenFaeAscendant());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(maralen.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Maralen permits one current-turn nonland spell for free up to the Elf and Faerie count")
    void permitsOneCurrentTurnSpellWithinSubtypeCount() {
        Permanent maralen = addReadyMaralen();
        Opt first = new Opt();
        Opt second = new Opt();
        GrizzlyBears tooExpensive = new GrizzlyBears();

        gd.addToExile(player2.getId(), first, maralen.getId());
        gd.addToExile(player2.getId(), second, maralen.getId());
        gd.addToExile(player2.getId(), tooExpensive, maralen.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, tooExpensive.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, first.getId());

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMaralen() {
        Permanent maralen = harness.addToBattlefieldAndReturn(player1, new MaralenFaeAscendant());
        maralen.setSummoningSick(false);
        return maralen;
    }
}
