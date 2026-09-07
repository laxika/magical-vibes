package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZealousPersecution;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartwoodStoryteller.class, ZealousPersecution.class, GrizzlyBears.class})
class HeartwoodStorytellerTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent of a noncreature spell's caster may draw")
    void eachOpponentOfCasterMayDraw() {
        harness.addToBattlefield(player1, new HeartwoodStoryteller());
        harness.setHand(player1, List.of(new ZealousPersecution()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        addZealousPersecutionMana(player1);

        castNoncreatureSpell(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveRemainingStack();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The caster is not offered a draw")
    void casterIsNotOfferedADraw() {
        harness.addToBattlefield(player1, new HeartwoodStoryteller());
        harness.setHand(player2, List.of(new ZealousPersecution()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addZealousPersecutionMana(player2);

        castNoncreatureSpell(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice = (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveRemainingStack();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new HeartwoodStoryteller());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void castNoncreatureSpell(com.github.laxika.magicalvibes.model.Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(caster, 0);
        harness.passBothPriorities();
    }

    private void addZealousPersecutionMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

    private void resolveRemainingStack() {
        while (!gd.stack.isEmpty() && gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }
    }
}
