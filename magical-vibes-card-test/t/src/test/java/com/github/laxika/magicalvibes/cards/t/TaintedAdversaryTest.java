package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.InfernalGrasp;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaintedAdversary.class, InfernalGrasp.class})
class TaintedAdversaryTest extends BaseCardTest {

    @Test
    void paysMultipleTimesAndCreatesTwiceAsManyDecayedZombies() {
        harness.setHand(player1, List.of(new TaintedAdversary()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        Permanent adversary = findPermanent(player1, "Tainted Adversary");
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(4).allSatisfy(zombie -> {
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
        });
    }

    @Test
    void mayDeclineWithoutAddingCountersOrCreatingTokens() {
        harness.setHand(player1, List.of(new TaintedAdversary()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent adversary = findPermanent(player1, "Tainted Adversary");
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(battlefield).noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void entersNormallyWhenNoPaymentIsAffordable() {
        Permanent adversary = harness.enterBattlefieldAndReturn(player1, new TaintedAdversary());
        resolveAllTriggers();

        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paymentCreatesSeparateRespondableTrigger() {
        Permanent adversary = enterWithOnePaymentAvailable();
        harness.handleXValueChosen(player1, 1);

        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();

        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
    }

    @Test
    void createdZombiesHaveCorrectCharacteristicsAndCannotBlockOrAttackImmediately() {
        enterWithOnePaymentAvailable();
        harness.handleXValueChosen(player1, 1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).hasSize(2).allSatisfy(zombie -> {
            assertThat(zombie.getCard().isToken()).isTrue();
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
            assertThat(bls.canBlock(gd, zombie)).isFalse();
            assertThat(als.canAttack(gd, zombie, player1.getId())).isFalse();
        });
    }

    @Test
    void stillCreatesTokensWhenRemovedInResponseToReflexiveTrigger() {
        Permanent adversary = enterWithOnePaymentAvailable();
        harness.handleXValueChosen(player1, 1);
        destroyAdversary(adversary);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tainted Adversary");
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        assertThat(adversary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayPayAndCreateTokensWhenRemovedBeforeEnterTriggerResolves() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent adversary = harness.enterBattlefieldAndReturn(player1, new TaintedAdversary());
        destroyAdversary(adversary);
        resolveAllTriggers();
        harness.handleXValueChosen(player1, 1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tainted Adversary");
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
    }

    @Test
    void attackingZombieWaitsForDelayedSacrificeTriggerToResolve() {
        enterWithOnePaymentAvailable();
        harness.handleXValueChosen(player1, 1);
        resolveAllTriggers();
        List<Permanent> zombies = findPermanents(player1, "Zombie");
        Permanent attacker = zombies.getFirst();
        attacker.setSummoningSick(false);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(attackerIndex));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        });
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(zombies);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker)
                .contains(zombies.get(1));
    }

    @Test
    void deathtouchKillsBlockerDespiteDealingLessThanLethalDamage() {
        addCreatureReady(player1, new TaintedAdversary());
        addCreatureReady(player2, new TaintedAdversary());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Tainted Adversary");
        harness.assertNotOnBattlefield(player2, "Tainted Adversary");
        harness.assertInGraveyard(player1, "Tainted Adversary");
        harness.assertInGraveyard(player2, "Tainted Adversary");
    }

    private Permanent enterWithOnePaymentAvailable() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent adversary = harness.enterBattlefieldAndReturn(player1, new TaintedAdversary());
        resolveAllTriggers();
        return adversary;
    }

    private void destroyAdversary(Permanent adversary) {
        harness.setHand(player2, List.of(new InfernalGrasp()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, adversary.getId());
    }
}
