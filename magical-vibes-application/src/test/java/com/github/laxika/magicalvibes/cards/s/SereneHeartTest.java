package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class SereneHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys Auras controlled by both players")
    void destroysAurasFromBothPlayers() {
        Permanent myBears = addCreature(player1, new GrizzlyBears());
        attachAura(player1, new HolyStrength(), myBears);
        Permanent theirBears = addCreature(player2, new GrizzlyBears());
        attachAura(player2, new Pacifism(), theirBears);

        castSereneHeart();

        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertInGraveyard(player2, "Pacifism");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves non-Aura enchantments alone")
    void leavesNonAuraEnchantmentsAlone() {
        harness.addToBattlefield(player1, new RuleOfLaw());

        castSereneHeart();

        harness.assertOnBattlefield(player1, "Rule of Law");
    }

    private void castSereneHeart() {
        harness.setHand(player1, List.of(new SereneHeart()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void attachAura(Player player, Card auraCard, Permanent host) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(host.getId());
        harness.getGameData().playerBattlefields.get(player.getId()).add(aura);
    }
}
