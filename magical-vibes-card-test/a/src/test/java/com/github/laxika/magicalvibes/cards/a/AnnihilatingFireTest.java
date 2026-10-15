package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GolgariLonglegs;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnnihilatingFire.class, GrizzlyBears.class, SerraAngel.class,
        GolgariLonglegs.class, AugerSpree.class})
class AnnihilatingFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to target player")
    void dealsThreeDamageToPlayer() {
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Creature killed by Annihilating Fire is exiled instead of going to graveyard")
    void killedCreatureIsExiled() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature that survives the damage is not exiled")
    void survivingCreatureIsNotExiled() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    void survivingCreatureIsExiledIfItDiesLaterThisTurn() {
        harness.addToBattlefield(player2, new GolgariLonglegs());
        harness.setHand(player1, List.of(new AnnihilatingFire(), new AugerSpree()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID targetId = harness.getPermanentId(player2, "Golgari Longlegs");

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertOnBattlefield(player2, "Golgari Longlegs");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Golgari Longlegs");
        harness.assertNotInGraveyard(player2, "Golgari Longlegs");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Golgari Longlegs"));
    }

    @Test
    void exileReplacementExpiresAfterTheTurn() {
        harness.addToBattlefield(player2, new GolgariLonglegs());
        harness.setHand(player1, List.of(new AnnihilatingFire(), new AugerSpree()));
        harness.addMana(player1, ManaColor.RED, 3);
        UUID targetId = harness.getPermanentId(player2, "Golgari Longlegs");

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Golgari Longlegs");
        harness.assertInGraveyard(player2, "Golgari Longlegs");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Golgari Longlegs"));
    }
}
