package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyggsCommandTest extends BaseCardTest {

    @Test
    void copyAndLifelinkModesResolve() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SyggsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(merfolk.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.MERFOLK));
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void drawAndTapStunModesResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new CoralMerfolk()));
        harness.setHand(player1, List.of(new SyggsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Coral Merfolk");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void copyModeRejectsAMerfolkNotControlledByTheCaster() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new SyggsCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(merfolk.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
