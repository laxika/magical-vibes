package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuxknightBreacher.class, DarksteelRelic.class, GrizzlyBears.class, Ornithopter.class, Forest.class})
class LuxknightBreacherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each other creature or artifact controlled")
    void entersWithCounterForEachOtherCreatureOrArtifact() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DarksteelRelic());
        harness.addToBattlefield(player1, new Ornithopter());

        castLuxknight();

        Permanent breacher = findPermanent(player1, "Luxknight Breacher");
        assertThat(breacher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts an artifact creature only once")
    void countsArtifactCreatureOnlyOnce() {
        harness.addToBattlefield(player1, new Ornithopter());

        castLuxknight();

        Permanent breacher = findPermanent(player1, "Luxknight Breacher");
        assertThat(breacher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts only matching permanents controlled by its controller")
    void ignoresNonmatchingAndOpposingPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new DarksteelRelic());

        castLuxknight();

        Permanent breacher = findPermanent(player1, "Luxknight Breacher");
        assertThat(breacher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castLuxknight() {
        harness.setHand(player1, List.of(new LuxknightBreacher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
