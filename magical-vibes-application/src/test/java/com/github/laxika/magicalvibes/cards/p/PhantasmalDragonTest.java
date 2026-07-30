package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhantasmalDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Phantasmal Dragon is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent dragon = new Permanent(new PhantasmalDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dragon);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, dragon.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertInGraveyard(player1, "Phantasmal Dragon");
    }

    @Test
    @DisplayName("Phantasmal Dragon is sacrificed when targeted by its controller's own spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent dragon = new Permanent(new PhantasmalDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dragon);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, dragon.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertInGraveyard(player1, "Phantasmal Dragon");
    }

    @Test
    @DisplayName("Phantasmal Dragon is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent dragon = new Permanent(new PhantasmalDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dragon);

        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent pyro = findPermanent(player2, "Prodigal Pyromancer");
        pyro.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyro),
                null, dragon.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertInGraveyard(player1, "Phantasmal Dragon");
    }

    @Test
    @DisplayName("Phantasmal Dragon stays on the battlefield while untargeted")
    void survivesUntargetedRemoval() {
        Permanent dragon = new Permanent(new PhantasmalDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dragon);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmal Dragon");
    }
}
