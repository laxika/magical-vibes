package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.Greed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerrakWarpedSengir.class, Greed.class, GrizzlyBears.class, ProdigalPyromancer.class})
class VerrakWarpedSengirTest extends BaseCardTest {

    @Test
    @DisplayName("May pay the life again to copy an ability with a life cost")
    void copiesLifePaidAbility() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        harness.addToBattlefield(player1, new Greed());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not trigger for an ability that did not require life")
    void doesNotTriggerWithoutLifePayment() {
        harness.addToBattlefield(player1, new VerrakWarpedSengir());
        addReadyPyromancer(player1);
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 1, null, player2.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyPyromancer(Player player) {
        Permanent perm = new Permanent(new ProdigalPyromancer());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
