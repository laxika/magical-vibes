package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfGrace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalArchon.class, DiscipleOfGrace.class})
@DisplayName("Cabal Archon")
class CabalArchonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing itself drains a target player for 2 life")
    void sacrificingItselfDrainsTargetPlayer() {
        addCreatureReady(player1, new CabalArchon());
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Cabal Archon");
    }

    @Test
    @DisplayName("Can sacrifice another Cleric")
    void canSacrificeAnotherCleric() {
        addCreatureReady(player1, new CabalArchon());
        Permanent otherCleric = addCreatureReady(player1, new DiscipleOfGrace());
        prepareAbility();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ActivatedAbilityCostChoice.class);
        harness.handlePermanentChosen(player1, otherCleric.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Cabal Archon");
        harness.assertInGraveyard(player1, "Disciple of Grace");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent archon = addCreatureReady(player1, new CabalArchon());
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, archon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
