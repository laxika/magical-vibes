package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightErrant;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarFalcon.class, EliteVanguard.class, GrizzlyBears.class, KnightErrant.class})
class WarFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack when controller controls a Soldier")
    void canAttackWithSoldier() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WarFalcon());
        addCreatureReady(player1, new EliteVanguard());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can attack when controller controls a Knight")
    void canAttackWithKnight() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WarFalcon());
        addCreatureReady(player1, new KnightErrant());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot attack without a Knight or Soldier")
    void cannotAttackWithoutKnightOrSoldier() {
        addCreatureReady(player1, new WarFalcon());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot attack when only the opponent controls a Soldier")
    void cannotAttackWhenOnlyOpponentControlsSoldier() {
        addCreatureReady(player1, new WarFalcon());
        addCreatureReady(player2, new EliteVanguard());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Soldier still permits attacking")
    void canAttackWithTappedSummoningSickSoldier() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WarFalcon());
        harness.addToBattlefieldAndReturn(player1, new EliteVanguard()).tap();

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A Soldier in hand and a Knight in the graveyard do not permit attacking")
    void cannotAttackWithSupportOutsideBattlefield() {
        addCreatureReady(player1, new WarFalcon());
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.setGraveyard(player1, List.of(new KnightErrant()));

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block without controlling a Knight or Soldier")
    void canBlockWithoutKnightOrSoldier() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WarFalcon());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "War Falcon");
    }
}
