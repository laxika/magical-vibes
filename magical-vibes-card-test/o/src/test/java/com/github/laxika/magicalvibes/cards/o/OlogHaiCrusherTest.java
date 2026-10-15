package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronclawOrcs;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OlogHaiCrusher.class, GrizzlyBears.class, MonssGoblinRaiders.class, IronclawOrcs.class})
class OlogHaiCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block without a Goblin or Orc")
    void cannotBlockWithoutGoblinOrOrc() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new OlogHaiCrusher());

        declareAttackers(player2, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block while controlling a Goblin")
    void canBlockWithGoblin() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new OlogHaiCrusher());
        addCreatureReady(player1, new MonssGoblinRaiders());

        declareAttackers(player2, List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can block while controlling an Orc")
    void canBlockWithOrc() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new OlogHaiCrusher());
        addCreatureReady(player1, new IronclawOrcs());

        declareAttackers(player2, List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can attack without controlling a Goblin or Orc")
    void canAttackWithoutGoblinOrOrc() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new OlogHaiCrusher());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }
}
