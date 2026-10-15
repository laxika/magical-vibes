package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraspOfTheHieromancer.class, GrizzlyBears.class, GaeasRevenge.class, TurnToFrog.class})
class GraspOfTheHieromancerTest extends BaseCardTest {

    private void enchant(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GraspOfTheHieromancer());
        aura.setAttachedTo(creature.getId());
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void boostsEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking taps a chosen creature the defending player controls")
    void tapsDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only creatures the defending player controls are legal targets")
    void ownCreaturesAreNotLegalTargets() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId())
                .doesNotContain(ownBears.getId(), attacker.getId());
    }

    @Test
    @DisplayName("No target selection when the defending player controls no creature")
    void noTriggerTargetWithoutDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("An unattached Grasp grants no attack trigger")
    void unattachedGraspDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GraspOfTheHieromancer());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("The enchanted creature's controller chooses the target even when the opponent controls Grasp")
    void enchantedCreaturesControllerChoosesTarget() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        enchant(attacker);
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability has the green enchanted creature as its source")
    void greenCreatureCanTargetGaeasRevenge() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);
        Permanent victim = addCreatureReady(player2, new GaeasRevenge());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Turn to Frog removes the previously granted attack ability")
    void losingAbilitiesRemovesGrantedAttackTrigger() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped defending creature is still a legal target")
    void tappedCreatureRemainsLegalTarget() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        enchant(attacker);
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        victim.tap();

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting Grasp attaches it and boosts an opposing creature")
    void canEnchantOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraspOfTheHieromancer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Grasp of the Hieromancer");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }
}
