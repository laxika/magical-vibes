package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DuskfangMentor.class, GrizzlyBears.class})
class DuskfangMentorTest extends BaseCardTest {

    @Test
    void entersWithLifelinkCounterOnTargetNonHumanCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DuskfangMentor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        Permanent lifelinkCreature = addCreatureReady(player1, new GrizzlyBears());
        lifelinkCreature.setCounterCount(CounterType.LIFELINK, 1);
        Permanent ordinaryCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mentor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lifelinkCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ordinaryCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
