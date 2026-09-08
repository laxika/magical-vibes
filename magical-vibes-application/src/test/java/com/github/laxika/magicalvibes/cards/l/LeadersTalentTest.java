package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LeadersTalent.class, Memnite.class, Shock.class})
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
