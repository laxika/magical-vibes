package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RhythmOfTheWild;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClamorShaman.class, SauroformHybrid.class, RhythmOfTheWild.class})
class ClamorShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Riot can put a +1/+1 counter on Clamor Shaman")
    void riotAddsCounter() {
        castShaman();

        harness.handleMayAbilityChosen(player1, true);

        Permanent shaman = findPermanent(player1, "Clamor Shaman");

        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shaman.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Declining Riot gives Clamor Shaman haste")
    void riotGivesHasteWhenDeclined() {
        castShaman();

        harness.handleMayAbilityChosen(player1, false);

        Permanent shaman = findPermanent(player1, "Clamor Shaman");

        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(shaman.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Attacking targets only a creature an opponent controls")
    void attackTriggerRestrictsTargets() {
        Permanent shaman = addCreatureReady(player1, new ClamorShaman());
        Permanent ownCreature = addCreatureReady(player1, new SauroformHybrid());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreature.getId())
                .doesNotContain(shaman.getId(), ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Riot haste allows Clamor Shaman to attack the turn it enters")
    void riotHasteAllowsImmediateAttack() {
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        castShaman();
        harness.handleMayAbilityChosen(player1, false);
        Permanent shaman = findPermanent(player1, "Clamor Shaman");

        declareAttackers(List.of(0));

        assertThat(shaman.isAttacking()).isTrue();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        assertThat(bls.canBlock(gd, opponentCreature)).isFalse();
    }

    @Test
    @DisplayName("Clamor Shaman can attack when the opponent controls no creatures")
    void attacksWithoutLegalTriggerTarget() {
        addCreatureReady(player1, new ClamorShaman());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The attack trigger prevents only the selected creature from blocking")
    void otherOpponentCreatureCanStillBlock() {
        addCreatureReady(player1, new ClamorShaman());
        Permanent target = addCreatureReady(player2, new SauroformHybrid());
        Permanent other = addCreatureReady(player2, new SauroformHybrid());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlock(gd, target)).isFalse();
        assertThat(bls.canBlock(gd, other)).isTrue();
    }

    @Test
    @DisplayName("Two instances of riot can each give a +1/+1 counter")
    void rhythmGrantsSecondIndependentRiotCounter() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());
        castShaman();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent shaman = findPermanent(player1, "Clamor Shaman");

        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(shaman.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Two instances of riot can give both a counter and haste")
    void rhythmAllowsCounterAndHaste() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());
        castShaman();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        Permanent shaman = findPermanent(player1, "Clamor Shaman");

        assertThat(shaman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shaman.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    private void castShaman() {
        harness.castFromHand(player1, new ClamorShaman(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotOnBattlefield(player1, "Clamor Shaman");
    }
}
