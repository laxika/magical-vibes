package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoureConfrontedByRobbers.class, GrizzlyBears.class, Island.class})
class YoureConfrontedByRobbersTest extends BaseCardTest {

    @Test
    void stallsUpToThreeTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareSpell();
        harness.castModalInstant(player1, 0, 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
    }

    @Test
    void callsForThreeSoldierTokens() {
        prepareSpell();
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(3);
    }

    @Test
    void stallForTimeCannotTargetNoncreaturePermanents() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YoureConfrontedByRobbers()));
        harness.addMana(player1, ManaColor.WHITE, 4);
    }
}
