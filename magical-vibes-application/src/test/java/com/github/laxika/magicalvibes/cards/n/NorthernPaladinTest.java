package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NorthernPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target black permanent")
    void resolvingDestroysTargetBlackPermanent() {
        setupPaladin();
        Permanent target = addPermanent(player2, new ScatheZombies());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Cannot target a non-black permanent")
    void cannotTargetNonBlackPermanent() {
        setupPaladin();
        Permanent target = addPermanent(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black permanent");
    }

    private void setupPaladin() {
        harness.addToBattlefield(player1, new NorthernPaladin());
        findPermanent(player1, "Northern Paladin").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
