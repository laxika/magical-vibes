package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PyrotechnicsTest extends BaseCardTest {

    @Test
    void dealsAll4DamageToSingleCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent giant = addToBattlefield(player2, new HillGiant());

        harness.castSorcery(player1, 0, Map.of(giant.getId(), 4));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // HillGiant is 3/3, 4 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
    }

    @Test
    void dividesDamageAmongTwoCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bears1 = addToBattlefield(player2, new GrizzlyBears());
        Permanent bears2 = addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, Map.of(bears1.getId(), 2, bears2.getId(), 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both are 2/2, both die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears1.getId()))
                .noneMatch(p -> p.getId().equals(bears2.getId()));
    }

    @Test
    void canDealAllDamageToPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 4));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void splitsDamageAmongCreatureAndPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(bears.getId(), 2, player2.getId(), 2));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Bears is 2/2, 2 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void damageAssignmentsMustSumTo4() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Pyrotechnics()));
        harness.addMana(player1, ManaColor.RED, 5);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        // Only assigning 2 damage — should fail
        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, Map.of(bears.getId(), 2))
        ).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addToBattlefield(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
