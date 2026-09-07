package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Melancholy.class, GrizzlyBears.class, FountainOfYouth.class})
class MelancholyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Melancholy taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Melancholy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachMelancholy(player1, creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again once Melancholy leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        Permanent aura = attachMelancholy(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to pay {B} sacrifices Melancholy")
    void decliningPaymentSacrificesAura() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachMelancholy(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Melancholy");
        harness.assertInGraveyard(player1, "Melancholy");
    }

    @Test
    @DisplayName("Paying {B} keeps Melancholy on the battlefield")
    void payingKeepsAura() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachMelancholy(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Melancholy");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachMelancholy(player1, creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Melancholy");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Melancholy")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Melancholy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachMelancholy(Player auraController, Permanent enchanted) {
        Permanent aura = new Permanent(new Melancholy());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
