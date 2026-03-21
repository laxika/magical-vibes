package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShivanFireTest extends BaseCardTest {

    @Test
    void deals2DamageToTargetCreatureUnkicked() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Hill Giant is 3/3 — survives 2 damage
        Permanent giant = addToBattlefield(player2, new HillGiant());

        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Hill Giant should still be on the battlefield with 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));
        Permanent survivingGiant = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(giant.getId()))
                .findFirst().orElseThrow();
        assertThat(survivingGiant.getDamage()).isEqualTo(2);
    }

    @Test
    void unkickedKillsCreatureWith2OrLessToughness() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Grizzly Bears is 2/2 — dies to 2 damage
        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    void kickedDeals4DamageToTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        // Base cost {R} + kicker {4} = 5 mana total
        harness.addMana(player1, ManaColor.RED, 5);

        // Hill Giant is 3/3 — dies to 4 damage
        Permanent giant = addToBattlefield(player2, new HillGiant());

        harness.castKickedInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    void goesToGraveyardAfterResolving() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShivanFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent bears = addToBattlefield(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shivan Fire"));
    }

    private Permanent addToBattlefield(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
