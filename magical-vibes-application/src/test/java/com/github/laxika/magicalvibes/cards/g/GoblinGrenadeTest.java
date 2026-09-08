package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinGrenade.class, GoblinPiker.class, CoralMerfolk.class, ChandraTheFirebrand.class,
        BoggartShenanigans.class})
class GoblinGrenadeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin deals 5 damage to target player")
    void dealsFiveDamageToPlayer() {
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), goblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertInGraveyard(player1, "Goblin Grenade");
    }

    @Test
    @DisplayName("Sacrificing a Goblin deals 5 damage to target creature")
    void dealsFiveDamageToCreature() {
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), goblin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Coral Merfolk");
        harness.assertInGraveyard(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Sacrificing a Goblin deals 5 damage to target planeswalker")
    void dealsFiveDamageToPlaneswalker() {
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, planeswalker.getId(), goblin.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertInGraveyard(player1, "Goblin Grenade");
    }

    @Test
    @DisplayName("A noncreature Goblin can pay the additional cost")
    void sacrificesNoncreatureGoblin() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), goblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.assertInGraveyard(player1, "Goblin Grenade");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Goblin creature")
    void cannotSacrificeNonGoblin() {
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Coral Merfolk");
        harness.assertInHand(player1, "Goblin Grenade");
    }

    @Test
    @DisplayName("Cannot sacrifice a Goblin controlled by an opponent")
    void cannotSacrificeOpponentsGoblin() {
        Permanent opponentsGoblin = addCreatureReady(player2, new GoblinPiker());

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, player2.getId(), opponentsGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Goblin Piker");
        harness.assertInHand(player1, "Goblin Grenade");
    }

    @Test
    @DisplayName("Does not damage a Goblin sacrificed as the target")
    void sacrificedTargetBecomesIllegal() {
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());

        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, goblin.getId(), goblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Goblin Piker");
        harness.assertInGraveyard(player1, "Goblin Grenade");
    }
}
