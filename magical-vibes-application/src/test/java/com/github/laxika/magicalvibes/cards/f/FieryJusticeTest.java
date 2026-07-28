package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieryJusticeTest extends BaseCardTest {

    @Test
    void dealsAll5DamageToOneCreatureAndOpponentGains5Life() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent giant = addToBattlefield(player2, new HillGiant());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId(), Map.of(giant.getId(), 5));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    void splitsDamageAmongCreatureAndPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId(), Map.of(bears.getId(), 2, player2.getId(), 3));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        // 3 damage to the opponent, then the same opponent gains 5 life.
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3 + 5);
    }

    @Test
    void damageAssignmentsMustSumTo5() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, player2.getId(), Map.of(bears.getId(), 3))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lifeGainTargetMustBeAnOpponent() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FieryJustice()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.castSorcery(player1, 0, player1.getId(), Map.of(bears.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addToBattlefield(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
