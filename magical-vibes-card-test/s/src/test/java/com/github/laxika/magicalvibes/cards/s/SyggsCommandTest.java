package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SyggsCommand.class, CoralMerfolk.class, GrizzlyBears.class})
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

    @Test
    void lifelinkAndDrawCanTargetTheSamePlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new CoralMerfolk()));
        harness.setHand(player1, List.of(new SyggsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2},
                List.of(player1.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.LIFELINK)).isFalse();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void copyIsCreatedBeforeLifelinkIsGranted() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.setHand(player1, List.of(new SyggsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(merfolk.getId(), player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo(merfolk.getCard().getName());
        assertThat(gqs.hasKeyword(gd, token, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void drawStillResolvesWhenTheCopyTargetLeavesTheBattlefield() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SyggsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(merfolk.getId(), player2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(merfolk);
        gd.playerGraveyards.get(player1.getId()).add(merfolk.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void anAlreadyTappedCreatureStillReceivesAStunCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.setLibrary(player1, List.of(new CoralMerfolk()));
        harness.setHand(player1, List.of(new SyggsCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3},
                List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Coral Merfolk");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void copyModeRejectsANonMerfolkControlledByTheCaster() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SyggsCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(creature.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
