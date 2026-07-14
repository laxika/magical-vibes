package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SouthernPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target red permanent")
    void resolvingDestroysTargetRedPermanent() {
        setupPaladin();
        Permanent target = addPermanent(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Cannot target a non-red permanent")
    void cannotTargetNonRedPermanent() {
        setupPaladin();
        Permanent target = addPermanent(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red permanent");
    }

    private void setupPaladin() {
        harness.addToBattlefield(player1, new SouthernPaladin());
        findPermanent(player1, "Southern Paladin").setSummoningSick(false);
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
