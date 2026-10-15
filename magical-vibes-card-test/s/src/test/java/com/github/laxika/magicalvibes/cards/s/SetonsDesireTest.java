package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PatrolHound;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SetonsDesire.class, PatrolHound.class})
class SetonsDesireTest extends BaseCardTest {

    @Test
    @DisplayName("Enchant creature cannot target a noncreature permanent")
    void enchantCreatureCannotTargetNoncreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new SetonsDesire());
        harness.setHand(player1, List.of(new SetonsDesire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new PatrolHound());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Threshold does not force blocks below seven cards in the Aura controller's graveyard")
    void thresholdDoesNotForceBlocksBelowSevenCards() {
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreatureReady(player2, new PatrolHound());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Threshold forces all able creatures to block enchanted creature")
    void thresholdForcesAllAbleCreaturesToBlock() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreatureReady(player2, new PatrolHound());
        addCreatureReady(player2, new PatrolHound());

        prepareDeclareBlockers();

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
        addCreatureReady(player2, new PatrolHound());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Threshold uses the Aura controller's graveyard for an opposing enchanted creature")
    void thresholdUsesAuraControllerGraveyardForOpposingCreature() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent attacker = addAttackingCreature(player2);
        attachAura(player1, attacker);
        addCreatureReady(player1, new PatrolHound());

        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");
    }

    @Test
    @DisplayName("Threshold does not force a creature unable to block")
    void thresholdDoesNotForceUnableBlocker() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        Permanent blocker = addCreatureReady(player2, new PatrolHound());
        blocker.tap();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Threshold stops forcing blocks below seven cards")
    void thresholdStopsForcingBlocksBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreatureReady(player2, new PatrolHound());

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Aura resolves attached to an opposing creature and boosts only that creature")
    void auraResolvesOnOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new PatrolHound());
        Permanent other = addCreatureReady(player2, new PatrolHound());
        harness.setHand(player1, List.of(new SetonsDesire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Seton's Desire");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reaching seven graveyard cards enables threshold before blockers are declared")
    void reachingSevenCardsEnablesThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreatureReady(player2, new PatrolHound());
        harness.setGraveyard(player1, graveyardWithSevenCards());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Threshold requires able blockers while allowing tapped creatures to remain unassigned")
    void thresholdRequiresOnlyAbleBlockers() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent attacker = addAttackingCreature(player1);
        attachAura(player1, attacker);
        addCreatureReady(player2, new PatrolHound());
        Permanent tapped = addCreatureReady(player2, new PatrolHound());
        tapped.tap();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("A blocker may choose either attacker when two threshold Auras require it to block")
    void competingThresholdRequirementsAllowEitherAttacker() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent first = addAttackingCreature(player1);
        Permanent second = addAttackingCreature(player1);
        attachAura(player1, first);
        attachAura(player1, second);
        addCreatureReady(player2, new PatrolHound());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new PatrolHound());
        creature.setAttacking(true);
        return creature;
    }

    private void attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new SetonsDesire());
        aura.setAttachedTo(creature.getId());
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new PatrolHound(), new PatrolHound(), new PatrolHound(), new PatrolHound(),
                new PatrolHound(), new PatrolHound(), new PatrolHound());
    }
}
