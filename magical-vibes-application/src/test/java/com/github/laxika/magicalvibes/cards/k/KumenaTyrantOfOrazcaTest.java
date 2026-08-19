package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KumenaTyrantOfOrazcaTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another Merfolk makes Kumena unblockable this turn")
    void tapAnotherMerfolkMakesKumenaUnblockable() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());

        harness.activateAbility(player1, battlefieldIndex(kumena), 0, null, null);
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        assertThat(kumena.isTapped()).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, kumena)).isTrue();
    }

    @Test
    @DisplayName("Tapping three Merfolk draws a card")
    void tapThreeMerfolkDrawsCard() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        Permanent merfolkA = addCreatureReady(player1, new CoralMerfolk());
        Permanent merfolkB = addCreatureReady(player1, new CoralMerfolk());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(kumena), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(kumena.isTapped()).isTrue();
        assertThat(merfolkA.isTapped()).isTrue();
        assertThat(merfolkB.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping five Merfolk puts a +1/+1 counter on each Merfolk")
    void tapFiveMerfolkPutsCountersOnEachMerfolk() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());
        Permanent merfolkA = addCreatureReady(player1, new CoralMerfolk());
        Permanent merfolkB = addCreatureReady(player1, new CoralMerfolk());
        Permanent merfolkC = addCreatureReady(player1, new CoralMerfolk());
        Permanent merfolkD = addCreatureReady(player1, new CoralMerfolk());

        harness.activateAbility(player1, battlefieldIndex(kumena), 2, null, null);
        harness.passBothPriorities();

        assertThat(kumena.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolkA.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolkB.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolkC.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(merfolkD.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability cannot be activated without another Merfolk")
    void cannotTapKumenaForItsOwnFirstAbility() {
        Permanent kumena = addCreatureReady(player1, new KumenaTyrantOfOrazca());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kumena), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
