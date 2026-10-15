package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreakneckRider.class, QuilledWolf.class})
class BreakneckRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms into Neck Breaker when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new BreakneckRider());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isTrue();
        assertThat(rider.getCard().getName()).isEqualTo("Neck Breaker");
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new BreakneckRider());
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(rider.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Neck Breaker transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoOrMoreSpellsWereCastLastTurn() {
        Permanent rider = addRiderTransformed();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isFalse();
        assertThat(rider.getCard().getName()).isEqualTo("Breakneck Rider");
    }

    @Test
    @DisplayName("Neck Breaker boosts and grants trample to attacking creatures you control")
    void attackingCreaturesYouControlAreBoostedAndGainTrample() {
        Permanent rider = addRiderTransformed();
        Permanent wolf = addCreatureReady(player1, new QuilledWolf());
        Permanent opponentWolf = addCreatureReady(player2, new QuilledWolf());

        markAttacking(player1, List.of(0, 1));
        markAttacking(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentWolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentWolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Neck Breaker does not affect nonattacking creatures you control")
    void nonattackingCreaturesAreNotAffected() {
        addRiderTransformed();
        Permanent wolf = addCreatureReady(player1, new QuilledWolf());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Transforms during the opponent's upkeep when no spells were cast")
    void transformsDuringOpponentsUpkeep() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new BreakneckRider());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("An opponent's spell prevents the front face's upkeep trigger")
    void opponentSpellPreventsTransformation() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new BreakneckRider());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(rider.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("One spell by each player does not transform Neck Breaker back")
    void oneSpellPerPlayerDoesNotTransformBack() {
        Permanent rider = addRiderTransformed();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(rider.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("No spells last turn leaves Neck Breaker on its back face")
    void noSpellsDoesNotTransformBack() {
        Permanent rider = addRiderTransformed();
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(rider.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The controller casting two spells also transforms Neck Breaker back")
    void controllerCastingTwoSpellsTransformsBack() {
        Permanent rider = addRiderTransformed();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Attacking bonuses end as soon as a creature stops attacking")
    void bonusesEndWhenCreatureStopsAttacking() {
        addRiderTransformed();
        Permanent wolf = addCreatureReady(player1, new QuilledWolf());
        wolf.setAttacking(true);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();

        wolf.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Removing Neck Breaker immediately removes its attacking bonuses")
    void bonusesEndWhenNeckBreakerLeavesBattlefield() {
        Permanent rider = addRiderTransformed();
        Permanent wolf = addCreatureReady(player1, new QuilledWolf());
        wolf.setAttacking(true);
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(rider);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addRiderTransformed() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new BreakneckRider());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        return rider;
    }

    private void markAttacking(Player player, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int index : attackerIndices) {
            battlefield.get(index).setAttacking(true);
        }
    }
}
