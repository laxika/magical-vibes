package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeadersTalent.class, Memnite.class, Shock.class, Disperse.class})
class LeadersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a chosen attacking creature")
    void putsCounterOnChosenAttacker() {
        harness.addToBattlefield(player1, new LeadersTalent());
        Permanent attacker = addCreatureReady();
        Permanent nonAttacker = addCreatureReady();

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("At level 2, gains 2 life when a counter-bearing creature leaves")
    void gainsLifeWhenCounterBearingCreatureLeaves() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        levelUp(talent, 0, 2);
        Permanent creature = addCreatureReady();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        destroyWithShock(creature);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("At level 2, does not gain life when a creature without counters leaves")
    void doesNotGainLifeWhenCreatureHasNoCounters() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        levelUp(talent, 0, 2);
        Permanent creature = addCreatureReady();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        destroyWithShock(creature);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("At level 3, puts a +1/+1 counter on each creature you control when you cast a spell")
    void putsCountersOnOwnCreaturesWhenCastingSpell() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        levelUp(talent, 0, 2);
        levelUp(talent, 1, 3);
        Permanent ownCreature = addCreatureReady();
        Permanent opposingCreature = addCreatureReady(player2, new Memnite());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void gainingClassLevelsDoesNotPlaceLevelCounters() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());

        levelUp(talent, 0, 2);

        assertThat(talent.getClassLevel()).isEqualTo(2);
        assertThat(talent.getCounterCount(CounterType.LEVEL)).isZero();

        levelUp(talent, 1, 3);

        assertThat(talent.getClassLevel()).isEqualTo(3);
        assertThat(talent.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void attackingWithMultipleCreaturesTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new LeadersTalent());
        Permanent first = addCreatureReady();
        Permanent second = addCreatureReady();

        declareAttackers(List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void levelOneDoesNotGainLifeForCounterBearingCreature() {
        harness.addToBattlefield(player1, new LeadersTalent());
        Permanent creature = addCreatureReady();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        destroyWithShock(creature);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void levelTwoRecognizesCountersOtherThanPlusOnePlusOneAndReturnToHand() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        levelUp(talent, 0, 2);
        Permanent creature = addCreatureReady();
        creature.setCounterCount(CounterType.CHARGE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        bounceWithDisperse(creature);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        harness.assertInHand(player1, "Memnite");
    }

    @Test
    void lifeGainTriggerResolvesAfterTalentLeavesBattlefield() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        levelUp(talent, 0, 2);
        Permanent creature = addCreatureReady();
        creature.setCounterCount(CounterType.CHARGE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        bounceWithDisperse(creature);
        assertThat(gd.stack).hasSize(1);
        bounceWithDisperse(talent);
        harness.assertInHand(player1, "Leader's Talent");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void spellCastTriggerResolvesAfterTalentLeavesBattlefield() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        levelUp(talent, 0, 2);
        levelUp(talent, 1, 3);
        Permanent creature = addCreatureReady();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        bounceWithDisperse(talent);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void levelTwoDoesNotTriggerForCastingSpell() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        levelUp(talent, 0, 2);
        Permanent creature = addCreatureReady();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opposingCreatureLeavingDoesNotGainLife() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        levelUp(talent, 0, 2);
        Permanent creature = addCreatureReady(player2, new Memnite());
        creature.setCounterCount(CounterType.CHARGE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        bounceWithDisperse(creature);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void cannotSkipLevelTwoOrRepeatLevelTwo() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LeadersTalent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        levelUp(talent, 0, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotGainLevelDuringCombat() {
        harness.addToBattlefield(player1, new LeadersTalent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void bounceWithDisperse(Permanent permanent) {
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, permanent.getId());
    }

    private Permanent addCreatureReady() {
        return addCreatureReady(player1, new Memnite());
    }

    private void destroyWithShock(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();
    }

    private void levelUp(Permanent talent, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        int talentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(talent);
        harness.activateAbility(player1, talentIndex, abilityIndex, null, null);
        resolveAllTriggers();
    }
}
