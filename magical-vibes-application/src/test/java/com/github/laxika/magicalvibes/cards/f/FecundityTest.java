package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.e.Expunge;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
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

@CardUsed({Fecundity.class, Expunge.class, Disenchant.class, ArgothianSwine.class, Forest.class, Pestilence.class, NevinyrralsDisk.class})
class FecundityTest extends BaseCardTest {

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

    @Test
    @DisplayName("Destroying a noncreature permanent does not trigger Fecundity")
    void noncreaturePermanentDoesNotTrigger() {
        Permanent fecundity = harness.addToBattlefieldAndReturn(player1, new Fecundity());

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, fecundity.getId());

        harness.assertNotOnBattlefield(player1, "Fecundity");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

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

    @Test
    @DisplayName("Fecundity's controller controls the trigger even when an opponent draws")
    void sourceControllerControlsOpponentDeathTrigger() {
        harness.addToBattlefield(player1, new Fecundity());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Expunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A queued draw still resolves after Fecundity is destroyed")
    void triggerSurvivesSourceRemoval() {
        Permanent fecundity = harness.addToBattlefieldAndReturn(player1, new Fecundity());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new Expunge(), new Disenchant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, fecundity.getId());
        harness.assertNotOnBattlefield(player1, "Fecundity");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Two creatures dying simultaneously each offer an independent draw")
    void simultaneousDeathsEachOfferDraw() {
        harness.addToBattlefield(player1, new Fecundity());
        harness.addToBattlefield(player1, new Pestilence());
        harness.addToBattlefield(player2, new ArgothianSwine());
        harness.addToBattlefield(player2, new ArgothianSwine());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        for (int activation = 0; activation < 3; activation++) {
            harness.activateAbility(player1, 1, null, null);
            harness.passBothPriorities();
        }
        harness.assertNotOnBattlefield(player2, "Argothian Swine");
        resolveAllTriggers();
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Fecundity triggers for a creature destroyed simultaneously with it")
    void sourceAndCreatureDestroyedSimultaneously() {
        harness.addToBattlefield(player1, new Fecundity());
        harness.addToBattlefield(player1, new NevinyrralsDisk());
        harness.addToBattlefield(player1, new ArgothianSwine());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Fecundity");
        harness.assertNotOnBattlefield(player1, "Argothian Swine");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

    private void destroyCreature(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Expunge()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
    }
}
