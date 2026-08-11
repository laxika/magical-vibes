package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SetonsDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreature(player1);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Threshold does not force blocks below seven cards in the Aura controller's graveyard")
    void thresholdDoesNotForceBlocksBelowSevenCards() {
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreature(player2);

        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Threshold forces all able creatures to block enchanted creature")
    void thresholdForcesAllAbleCreaturesToBlock() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreature(player2);
        addCreature(player2);

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
    }

    @Test
    @DisplayName("Opponent graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreature(player2);

        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Threshold stops forcing blocks below seven cards")
    void thresholdStopsForcingBlocksBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreature(player2);

        beginBlockerDeclaration();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
    }

    private Permanent addCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreature(player);
        creature.setAttacking(true);
        return creature;
    }

    private void attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new SetonsDesire());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
