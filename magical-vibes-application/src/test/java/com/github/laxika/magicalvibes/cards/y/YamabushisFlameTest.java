package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.h.HearthKami;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YamabushisFlame.class, HearthKami.class, MossKami.class, HealingSalve.class, GlacialRay.class})
class YamabushisFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player")
    void dealsThreeDamageToPlayer() {
        harness.setHand(player1, List.of(new YamabushisFlame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Creature killed by Yamabushi's Flame is exiled instead of going to the graveyard")
    void killedCreatureIsExiled() {
        harness.addToBattlefield(player2, new HearthKami());
        harness.setHand(player1, List.of(new YamabushisFlame()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Hearth Kami");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Hearth Kami");
        harness.assertNotInGraveyard(player2, "Hearth Kami");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hearth Kami"));
    }

    @Test
    @DisplayName("Creature that survives the damage is not exiled")
    void survivingCreatureIsNotExiled() {
        harness.addToBattlefield(player2, new MossKami());
        harness.setHand(player1, List.of(new YamabushisFlame()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Moss Kami");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Moss Kami");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Moss Kami"));
    }

    @Test
    @DisplayName("Prevented damage does not cause a later death to be exiled")
    void preventedDamageDoesNotMarkCreatureForExile() {
        harness.addToBattlefield(player2, new HearthKami());
        UUID targetId = harness.getPermanentId(player2, "Hearth Kami");

        harness.setHand(player1, List.of(new YamabushisFlame()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hearth Kami");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Hearth Kami"));

        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Hearth Kami");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Hearth Kami"));
    }
}
