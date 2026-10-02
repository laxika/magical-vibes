package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcquiredMutation.class, GrizzlyBears.class, Boomerang.class, JaceBeleren.class})
class AcquiredMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and is goaded")
    void enchantedCreatureGetsBoostAndGoaded() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachMutation(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isOne();
    }

    @Test
    @DisplayName("Attacking enchanted creature gives the defending player two rad counters")
    void attackingEnchantedCreatureGivesDefendingPlayerRadCounters() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMutation(player1, creature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
    }

    @Test
    void resolvingAuraAttachesToOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AcquiredMutation()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Acquired Mutation").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isOne();
    }

    @Test
    void opponentsEnchantedCreatureGivesAuraControllerRadCounters() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachMutation(player1, creature);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isNull();
    }

    @Test
    void unenchantedCreatureAttackingDoesNotGiveRadCounters() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        enchanted.tap();
        addCreatureReady(player1, new GrizzlyBears());
        attachMutation(player1, enchanted);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters).isEmpty();
    }

    @Test
    void removingAuraEndsBoostAndGoad() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachMutation(player1, creature);
        bounce(aura);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(als.getMustAttackRequirementCount(gd, creature)).isZero();
    }

    @Test
    void attackTriggerResolvesAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachMutation(player1, creature);
        declareAttackers(List.of(0));

        bounce(aura);
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void attackingPlaneswalkerGivesItsControllerRadCounters() {
        attackPlaneswalker(false);
    }

    @Test
    void attackTriggerStillGivesRadCountersAfterAttackedPlaneswalkerLeaves() {
        attackPlaneswalker(true);
    }

    private void attackPlaneswalker(boolean bounceBeforeResolution) {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMutation(player2, creature);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        if (bounceBeforeResolution) {
            bounce(planeswalker);
        }
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
    }

    private void bounce(Permanent permanent) {
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
        assertThat(gqs.findPermanentById(gd, permanent.getId())).isNull();
    }

    private Permanent attachMutation(com.github.laxika.magicalvibes.model.Player controller,
                                     Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new AcquiredMutation());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
