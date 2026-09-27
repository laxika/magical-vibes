package com.github.laxika.magicalvibes.cards.p;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhenomenonInvestigators.class, GrizzlyBears.class, Shock.class})
class PhenomenonInvestigatorsTest extends BaseCardTest {

    @Test
    @DisplayName("Believe creates a Horror when a nontoken creature you control dies")
    void believeCreatesHorrorOnNontokenCreatureDeath() {
        castAndChoose("Believe");
        harness.addToBattlefield(player1, new GrizzlyBears());

        killCreature(player1, player2);

        assertThat(findPermanents(player1, "Horror")).hasSize(1);
    }

    @Test
    @DisplayName("Doubt may return an owned nonland permanent and draw a card")
    void doubtReturnsOwnedNonlandPermanentAndDraws() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        castAndChoose("Doubt");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerHands.get(player1.getId())).contains(bears.getCard(), drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Doubt does nothing when its optional return is declined")
    void doubtCanBeDeclined() {
        castAndChoose("Doubt");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    private void castAndChoose(String mode) {
        harness.setHand(player1, List.of(new PhenomenonInvestigators()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    private void killCreature(com.github.laxika.magicalvibes.model.Player targetController,
                              com.github.laxika.magicalvibes.model.Player caster) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(targetController, "Grizzly Bears");
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
