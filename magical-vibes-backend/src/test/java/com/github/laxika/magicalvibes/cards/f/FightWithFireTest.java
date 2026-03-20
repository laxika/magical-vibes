package com.github.laxika.magicalvibes.cards.f;

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

class FightWithFireTest extends BaseCardTest {

    @Test
    void deals5DamageToTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // GrizzlyBears is 2/2, 5 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    void unkickedGoesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fight with Fire"));
    }

    @Test
    void kickedDeals10DamageDividedAmongCreatures() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        // Base cost {2}{R} + kicker {5}{R} = 9 mana total
        harness.addMana(player1, ManaColor.RED, 9);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = addToBattlefield(player2, new HillGiant());

        harness.castKickedSorcery(player1, 0, Map.of(
                bears.getId(), 4,
                giant.getId(), 6
        ));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // GrizzlyBears is 2/2, 4 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        // HillGiant is 3/3, 6 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
    }

    @Test
    void kickedCanDealAllDamageToPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        // Base cost {2}{R} + kicker {5}{R} = 9 mana total
        harness.addMana(player1, ManaColor.RED, 9);

        // A creature must exist so the base spell is considered playable
        addToBattlefield(player2, new GrizzlyBears());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castKickedSorcery(player1, 0, Map.of(
                player2.getId(), 10
        ));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 10);
    }

    @Test
    void kickedCanSplitDamageAmongCreaturesAndPlayers() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        // Base cost {2}{R} + kicker {5}{R} = 9 mana total
        harness.addMana(player1, ManaColor.RED, 9);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castKickedSorcery(player1, 0, Map.of(
                bears.getId(), 3,
                player2.getId(), 7
        ));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Bears is 2/2, 3 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 7);
    }

    @Test
    void kickedDamageAssignmentsMustSumTo10() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        // Only assigning 5 damage — should fail
        assertThatThrownBy(() ->
                harness.castKickedSorcery(player1, 0, Map.of(bears.getId(), 5))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedDamageAssignmentsMustBePositive() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new FightWithFire()));
        harness.addMana(player1, ManaColor.RED, 9);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.castKickedSorcery(player1, 0, Map.of(
                        bears.getId(), 0,
                        player2.getId(), 10
                ))
        ).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addToBattlefield(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
