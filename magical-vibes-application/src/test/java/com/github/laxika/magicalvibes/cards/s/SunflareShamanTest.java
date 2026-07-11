package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.EarthElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SunflareShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X to any target and X to itself, X = Elemental cards in your graveyard")
    void dealsElementalCountToTargetAndSelf() {
        Permanent shaman = addReadyShaman(player1);
        gd.playerGraveyards.get(player1.getId()).add(new AirElemental());
        gd.playerGraveyards.get(player1.getId()).add(new EarthElemental());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // X = 2: player2 takes 2, and the 2/1 shaman takes 2 and dies to its own damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Sunflare Shaman");
        harness.assertInGraveyard(player1, "Sunflare Shaman");
    }

    @Test
    @DisplayName("Only Elemental cards in the graveyard count toward X")
    void nonElementalCardsDoNotCount() {
        addReadyShaman(player1);
        gd.playerGraveyards.get(player1.getId()).add(new AirElemental());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears()); // Bear, not counted
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // X = 1: only the Air Elemental counts. The 2/1 shaman takes 1 and dies to its own damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Sunflare Shaman");
    }

    @Test
    @DisplayName("With no Elemental cards in graveyard X is 0 — no damage, source survives")
    void zeroElementalsDealsNoDamage() {
        Permanent shaman = addReadyShaman(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(shaman.getMarkedDamage()).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Sunflare Shaman");
    }

    @Test
    @DisplayName("Can target a creature — deals X damage to it")
    void dealsDamageToTargetCreature() {
        addReadyShaman(player1);
        gd.playerGraveyards.get(player1.getId()).add(new AirElemental());
        gd.playerGraveyards.get(player1.getId()).add(new EarthElemental());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // X = 2 kills the 2/2 Grizzly Bears.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addReadyShaman(Player player) {
        SunflareShaman card = new SunflareShaman();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
