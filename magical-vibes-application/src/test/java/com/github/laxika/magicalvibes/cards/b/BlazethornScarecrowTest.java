package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.j.JuvenileGloomwidow;
import com.github.laxika.magicalvibes.cards.r.RunesOfTheDeus;
import com.github.laxika.magicalvibes.cards.t.TattermungeManiac;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlazethornScarecrow.class, BloodmarkMentor.class, JuvenileGloomwidow.class,
        RunesOfTheDeus.class, BarrentonMedic.class, TattermungeManiac.class})
class BlazethornScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Has neither haste nor wither with no red or green creature")
    void noKeywordsByDefault() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Has haste while you control a red creature")
    void hasHasteWithRedCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new BloodmarkMentor()); // red

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Has wither while you control a green creature")
    void hasWitherWithGreenCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new JuvenileGloomwidow()); // green

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("An opponent's red creature does not grant haste")
    void opponentRedCreatureDoesNotGrantHaste() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player2, new BloodmarkMentor()); // red, opponent

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses haste when the red creature leaves the battlefield")
    void losesHasteWhenRedCreatureLeaves() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        Permanent red = harness.addToBattlefieldAndReturn(player1, new BloodmarkMentor());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(red);

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
    }

    @Test
    void opponentGreenCreatureDoesNotGrantWither() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player2, new JuvenileGloomwidow());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    void oneRedGreenCreatureGrantsBothAbilities() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TattermungeManiac());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(creature);

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    void abilitiesAreIndependentAndUpdateWhenGreenCreatureLeaves() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new BloodmarkMentor());
        Permanent green = harness.addToBattlefieldAndReturn(player1, new JuvenileGloomwidow());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(green);

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    void redAndGreenNoncreatureDoesNotGrantEitherAbility() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RunesOfTheDeus());
        aura.setAttachedTo(scarecrow.getId());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    void canAttackImmediatelyWithRedCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new BloodmarkMentor());

        assertThat(scarecrow.isSummoningSick()).isTrue();
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    void witherDealsCountersToBlocker() {
        addCreatureReady(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new JuvenileGloomwidow());
        Permanent blocker = addCreatureReady(player2, new BarrentonMedic());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    void dealsNormalDamageWithoutGreenCreature() {
        addCreatureReady(player1, new BlazethornScarecrow());
        Permanent blocker = addCreatureReady(player2, new BarrentonMedic());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void witherDealsNormalLifeLossToPlayer() {
        addCreatureReady(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new JuvenileGloomwidow());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
