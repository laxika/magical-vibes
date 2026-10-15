package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.m.MosscoatGoriak;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HornbashMentor.class, MosscoatGoriak.class, AlmightyBrushwagg.class})
class HornbashMentorTest extends BaseCardTest {

    @Test
    void entersWithTrampleCounterOnTargetNonHumanCreature() {
        Permanent target = addCreatureReady(player1, new MosscoatGoriak());
        harness.setHand(player1, List.of(new HornbashMentor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotTargetHumanCreature() {
        Permanent target = addCreatureReady(player1, new HornbashMentor());
        harness.setHand(player1, List.of(new HornbashMentor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityPutsCountersOnlyOnCreaturesWithTrample() {
        Permanent mentor = addCreatureReady(player1, new HornbashMentor());
        Permanent tramplingCreature = addCreatureReady(player1, new MosscoatGoriak());
        tramplingCreature.setCounterCount(CounterType.TRAMPLE, 1);
        Permanent ordinaryCreature = addCreatureReady(player1, new MosscoatGoriak());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(tramplingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ordinaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetOpponentsNonHumanCreature() {
        Permanent target = addCreatureReady(player2, new MosscoatGoriak());
        harness.setHand(player1, List.of(new HornbashMentor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnterWithoutAnyLegalTarget() {
        harness.castFromHand(player1, new HornbashMentor(), "{2}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hornbash Mentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activatedAbilityIncludesNaturalTrampleAndHumanCreaturesButNotOpponentsCreatures() {
        Permanent mentor = addCreatureReady(player1, new HornbashMentor());
        mentor.setCounterCount(CounterType.TRAMPLE, 1);
        Permanent naturalTrampler = addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent opponentsTrampler = addCreatureReady(player2, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(mentor.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(naturalTrampler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentsTrampler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activatedAbilityChecksTrampleAtResolutionAndSurvivesSourceRemoval() {
        Permanent mentor = addCreatureReady(player1, new HornbashMentor());
        Permanent losesTrample = addCreatureReady(player1, new MosscoatGoriak());
        losesTrample.setCounterCount(CounterType.TRAMPLE, 1);
        Permanent gainsTrample = addCreatureReady(player1, new MosscoatGoriak());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        losesTrample.setCounterCount(CounterType.TRAMPLE, 0);
        gainsTrample.setCounterCount(CounterType.TRAMPLE, 1);
        gd.playerBattlefields.get(player1.getId()).remove(mentor);
        harness.passBothPriorities();

        assertThat(losesTrample.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gainsTrample.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
