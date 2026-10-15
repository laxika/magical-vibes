package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.j.JaceArchitectOfThought;
import com.github.laxika.magicalvibes.cards.d.DramaticRescue;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({ExplosiveImpact.class, SerraAngel.class, JaceArchitectOfThought.class, DramaticRescue.class})
class ExplosiveImpactTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to target player")
    void dealsFiveDamageToPlayer() {
        harness.setHand(player1, List.of(new ExplosiveImpact()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Deals 5 damage to a target creature, killing a 4/4")
    void killsFourToughnessCreature() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new ExplosiveImpact()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new ExplosiveImpact()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Explosive Impact");
    }

    @Test
    void damagesPlaneswalkerWithoutDamagingItsController() {
        harness.addToBattlefield(player2, new JaceArchitectOfThought());
        harness.setHand(player1, List.of(new ExplosiveImpact()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Jace, Architect of Thought"));

        harness.assertNotOnBattlefield(player2, "Jace, Architect of Thought");
        harness.assertInGraveyard(player2, "Jace, Architect of Thought");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotDamagePlayersWhenCreatureTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new ExplosiveImpact()));
        harness.setHand(player2, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
        harness.assertInHand(player2, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player1, "Explosive Impact");
    }
}
