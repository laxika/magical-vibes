package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecklessEmbermageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 to a target player and 1 to itself; the 2/2 survives")
    void dealsOneToPlayerAndSelf() {
        Permanent embermage = addReadyEmbermage(player1);
        addRedMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(embermage.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Reckless Embermage");
    }

    @Test
    @DisplayName("Can target a creature — deals 1 damage to it")
    void dealsOneToTargetCreature() {
        addReadyEmbermage(player1);
        addRedMana(player1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // 1 damage marked on the 2/2 Bears — it survives.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ability has no tap cost — repeating it twice kills the Embermage")
    void repeatedActivationsKillItself() {
        addReadyEmbermage(player1);
        addRedMana(player1);
        addRedMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Two points of self-damage on the 2/2 send it to the graveyard.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Reckless Embermage");
        harness.assertInGraveyard(player1, "Reckless Embermage");
    }

    private void addRedMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyEmbermage(Player player) {
        RecklessEmbermage card = new RecklessEmbermage();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
