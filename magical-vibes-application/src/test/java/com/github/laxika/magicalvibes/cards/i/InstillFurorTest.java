package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InstillFuror.class, Watchwolf.class})
class InstillFurorTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast targeting a creature")
    void castsAndAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        harness.setHand(player1, List.of(new InstillFuror()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Instill Furor");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot be cast targeting a noncreature permanent")
    void cannotEnchantANoncreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new InstillFuror());
        harness.setHand(player1, List.of(new InstillFuror()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature is sacrificed at its controller's end step if it did not attack")
    void sacrificesNonAttackerAtControllerEndStep() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachAura(player1, creature);

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Watchwolf");
    }

    @Test
    @DisplayName("Enchanted creature survives its controller's end step if it attacked")
    void sparesAttacker() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachAura(player1, creature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("The trigger uses the enchanted creature controller's end step")
    void triggersDuringEnchantedCreatureControllersEndStep() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        attachAura(player1, creature);

        advanceToEndStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        advanceToEndStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Removing the Aura after the trigger does not stop the sacrifice")
    void triggerSurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        Permanent aura = attachAura(player1, creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("The ability still triggers when the enchanted creature attacked")
    void attackerStillTriggersAtEndStep() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        attachAura(player1, creature);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(creature.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("An attack before Instill Furor was attached still prevents sacrifice")
    void remembersAttackBeforeAuraWasCast() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> declareAttackers(player1,
                        List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature))));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new InstillFuror()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, harness::passBothPriorities);

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(findPermanent(player1, "Instill Furor").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The Aura goes to its owner's graveyard when the opposing creature is sacrificed")
    void sacrificeRemovesAuraToItsOwnersGraveyard() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        attachAura(player1, creature);

        advanceToEndStep(player2);

        harness.assertInGraveyard(player2, "Watchwolf");
        harness.assertInGraveyard(player1, "Instill Furor");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new InstillFuror());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
