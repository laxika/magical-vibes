package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FavorableDestiny;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.h.HallOfGemstone;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SereneHeart.class, FavorableDestiny.class, GiantMantis.class, HallOfGemstone.class,
        Pacifism.class})
class SereneHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys Auras controlled by both players")
    void destroysAurasFromBothPlayers() {
        Permanent myMantis = addCreatureReady(player1, new GiantMantis());
        attachAura(player1, new FavorableDestiny(), myMantis);
        Permanent theirMantis = addCreatureReady(player2, new GiantMantis());
        attachAura(player2, new Pacifism(), theirMantis);

        castSereneHeart();

        harness.assertNotOnBattlefield(player1, "Favorable Destiny");
        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertInGraveyard(player1, "Favorable Destiny");
        harness.assertInGraveyard(player2, "Pacifism");
        harness.assertOnBattlefield(player1, "Giant Mantis");
        harness.assertOnBattlefield(player2, "Giant Mantis");
    }

    @Test
    @DisplayName("Destroys an Aura attached to a creature controlled by the other player")
    void destroysAuraAttachedToAnotherPlayersCreature() {
        Permanent theirMantis = addCreatureReady(player2, new GiantMantis());
        attachAura(player1, new Pacifism(), theirMantis);

        castSereneHeart();

        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertOnBattlefield(player2, "Giant Mantis");
    }

    @Test
    @DisplayName("Leaves non-Aura enchantments alone")
    void leavesNonAuraEnchantmentsAlone() {
        harness.addToBattlefield(player1, new HallOfGemstone());

        castSereneHeart();

        harness.assertOnBattlefield(player1, "Hall of Gemstone");
    }

    private void castSereneHeart() {
        harness.castFromHand(player1, new SereneHeart(), "{1}{G}");
        harness.passBothPriorities();
    }

    private void attachAura(Player player, Card auraCard, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, auraCard);
        aura.setAttachedTo(host.getId());
    }
}
