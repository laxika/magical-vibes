package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BootNipper;
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

@CardUsed({DuskfangMentor.class, BootNipper.class})
class DuskfangMentorTest extends BaseCardTest {

    @Test
    void entersWithLifelinkCounterOnTargetNonHumanCreature() {
        Permanent target = addCreatureReady(player1, new BootNipper());
        harness.setHand(player1, List.of(new DuskfangMentor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void cannotTargetHumanCreature() {
        Permanent target = addCreatureReady(player1, new DuskfangMentor());
        harness.setHand(player1, List.of(new DuskfangMentor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityPutsCountersOnlyOnCreaturesWithLifelink() {
        Permanent mentor = addCreatureReady(player1, new DuskfangMentor());
        Permanent lifelinkCreature = addCreatureReady(player1, new BootNipper());
        lifelinkCreature.setCounterCount(CounterType.LIFELINK, 1);
        Permanent ordinaryCreature = addCreatureReady(player1, new BootNipper());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lifelinkCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ordinaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetOpponentsNonHumanCreature() {
        Permanent target = addCreatureReady(player2, new BootNipper());
        harness.setHand(player1, List.of(new DuskfangMentor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityIncludesHumansWithLifelinkButNotOpponentsCreatures() {
        Permanent mentor = addCreatureReady(player1, new DuskfangMentor());
        mentor.setCounterCount(CounterType.LIFELINK, 1);
        Permanent opponent = addCreatureReady(player2, new BootNipper());
        opponent.setCounterCount(CounterType.LIFELINK, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(mentor.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activatedAbilityChecksLifelinkAtResolution() {
        addCreatureReady(player1, new DuskfangMentor());
        Permanent losesLifelink = addCreatureReady(player1, new BootNipper());
        losesLifelink.setCounterCount(CounterType.LIFELINK, 1);
        Permanent gainsLifelink = addCreatureReady(player1, new BootNipper());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        losesLifelink.setCounterCount(CounterType.LIFELINK, 0);
        gainsLifelink.setCounterCount(CounterType.LIFELINK, 1);
        harness.passBothPriorities();

        assertThat(losesLifelink.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gainsLifelink.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
