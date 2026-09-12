package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.e.Expunge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fecundity.class, Expunge.class, ArgothianSwine.class, Forest.class})
class FecundityTest extends BaseCardTest {

    // ===== Dying creature's controller (an opponent) may draw =====

    @Test
    @DisplayName("When an opponent's creature dies, that opponent may draw (accept)")
    void opponentsCreatureDiesOpponentDraws() {
        harness.addToBattlefield(player1, new Fecundity());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());

        harness.setLibrary(player2, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        destroyCreature(player1, creature);

        // The DYING creature's controller (player2), not Fecundity's controller, is offered the draw.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Dying creature's controller may decline the draw")
    void controllerMayDecline() {
        harness.addToBattlefield(player1, new Fecundity());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());

        harness.setLibrary(player2, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        destroyCreature(player1, creature);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }

    // ===== Also fires for the controller's own creatures =====

    @Test
    @DisplayName("When Fecundity's controller's creature dies, that controller may draw")
    void ownCreatureDiesControllerDraws() {
        harness.addToBattlefield(player1, new Fecundity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSwine());

        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        destroyCreature(player2, creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Each Fecundity creates a separate draw choice for one creature's death")
    void eachFecundityTriggersSeparately() {
        harness.addToBattlefield(player1, new Fecundity());
        harness.addToBattlefield(player1, new Fecundity());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());

        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        destroyCreature(player1, creature);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 2);
    }

    // ===== Helpers =====

    private void destroyCreature(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Expunge()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
    }
}
