package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ThunderLasso.class, GrizzlyBears.class})
class ThunderLassoTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Thunder Lasso attaches it to a target creature you control")
    void enteringAttachesToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThunderLasso()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lasso = findPermanent(player1, "Thunder Lasso");
        assertThat(lasso.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking with the equipped creature taps a creature defending player controls")
    void attackingTapsDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent lasso = harness.addToBattlefieldAndReturn(player1, new ThunderLasso());
        lasso.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature controlled by the attacker")
    void attackTriggerOnlyTargetsDefendingCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent lasso = harness.addToBattlefieldAndReturn(player1, new ThunderLasso());
        lasso.setAttachedTo(attacker.getId());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(defendingCreature.getId())
                .doesNotContain(attacker.getId(), ownCreature.getId());
    }

    @Test
    @DisplayName("An unattached Thunder Lasso does not trigger when a creature attacks")
    void unattachedLassoDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new ThunderLasso());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Equip pays two generic mana and transfers the boost to the new creature")
    void equipMovesAttachmentAndBoost() {
        Permanent lasso = harness.addToBattlefieldAndReturn(player1, new ThunderLasso());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        lasso.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(lasso.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();

        assertThat(lasso.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Thunder Lasso enters unattached when its controller has no creatures")
    void entersWithoutCreatureToAttachTo() {
        harness.setHand(player1, List.of(new ThunderLasso()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Thunder Lasso").getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking with a different creature does not trigger the attached Lasso")
    void unequippedAttackerDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent lasso = harness.addToBattlefieldAndReturn(player1, new ThunderLasso());
        lasso.setAttachedTo(equipped.getId());
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(defender.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger resolves after Thunder Lasso leaves the battlefield")
    void attackTriggerSurvivesEquipmentRemoval() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent lasso = harness.addToBattlefieldAndReturn(player1, new ThunderLasso());
        lasso.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lasso);
        gd.playerGraveyards.get(player1.getId()).add(lasso.getCard());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The equipment controller chooses the target even when the attacker has another controller")
    void equipmentControllerChoosesTargetForOpponentsAttacker() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent lasso = harness.addToBattlefieldAndReturn(player1, new ThunderLasso());
        lasso.setAttachedTo(attacker.getId());
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }
}
