package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlurryOfWingsTest extends BaseCardTest {

    private long birdSoldierCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Bird Soldier".equals(p.getCard().getName()))
                .count();
    }

    private void addManaForCast(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }

    private void markAttacking(Player player, String cardName) {
        Permanent perm = gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
        perm.setAttacking(true);
    }

    @Test
    @DisplayName("Creates one flying Bird Soldier token per attacking creature, across all players")
    void createsTokenPerAttacker() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        markAttacking(player1, "Grizzly Bears");
        markAttacking(player2, "Grizzly Bears");
        markAttacking(player2, "Serra Angel");

        harness.setHand(player1, List.of(new FlurryOfWings()));
        addManaForCast(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // X = 3 attacking creatures → 3 tokens, all under the caster's control.
        assertThat(birdSoldierCount(player1)).isEqualTo(3);
        assertThat(birdSoldierCount(player2)).isZero();
    }

    @Test
    @DisplayName("Creates no tokens when there are no attacking creatures")
    void createsNoTokensWithoutAttackers() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // present but not attacking

        harness.setHand(player1, List.of(new FlurryOfWings()));
        addManaForCast(player1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(birdSoldierCount(player1)).isZero();
    }
}
