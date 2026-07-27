package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DarajaGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing destroys target black creature")
    void sacrificingDestroysTargetBlackCreature() {
        setupGriffin();
        Permanent target = addPermanent(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Griffin is sacrificed as a cost.
        harness.assertNotOnBattlefield(player1, "Daraja Griffin");
        harness.assertInGraveyard(player1, "Daraja Griffin");
        // Target black creature is destroyed.
        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Cannot target a nonblack creature")
    void cannotTargetNonBlackCreature() {
        setupGriffin();
        Permanent target = addPermanent(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    private void setupGriffin() {
        harness.addToBattlefield(player1, new DarajaGriffin());
        findPermanent(player1, "Daraja Griffin").setSummoningSick(false);
        harness.forceActivePlayer(player1);
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
