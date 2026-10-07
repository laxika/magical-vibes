package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.h.HeadwaterSentries;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarOfExtinction.class, Forest.class, Island.class, HeadwaterSentries.class, JaceCunningCastaway.class})
class StarOfExtinctionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting targets a land and puts spell on the stack")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new HeadwaterSentries());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Headwater Sentries");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Deals 20 damage to creatures on both sides, killing them")
    void deals20DamageToAllCreatures() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new HeadwaterSentries());
        harness.addToBattlefield(player2, new HeadwaterSentries());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Both creatures should be dead
        harness.assertNotOnBattlefield(player1, "Headwater Sentries");
        harness.assertNotOnBattlefield(player2, "Headwater Sentries");
        // Creatures should be in graveyards
        harness.assertInGraveyard(player1, "Headwater Sentries");
        harness.assertInGraveyard(player2, "Headwater Sentries");
    }

    @Test
    @DisplayName("Deals 20 damage to planeswalkers, killing them")
    void deals20DamageToPlaneswalkers() {
        harness.addToBattlefield(player1, new Forest());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceCunningCastaway());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jace, Cunning Castaway");
        harness.assertInGraveyard(player2, "Jace, Cunning Castaway");
    }

    @Test
    @DisplayName("Deals exactly twenty damage to surviving creatures and planeswalkers")
    void dealsExactlyTwentyDamage() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HeadwaterSentries());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 16);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceCunningCastaway());
        planeswalker.setCounterCount(CounterType.LOYALTY, 21);
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Headwater Sentries");
        assertThat(creature.getMarkedDamage()).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Jace, Cunning Castaway");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @CardUsed({DarksteelCitadel.class})
    @DisplayName("An indestructible target land survives but creatures still take damage")
    void indestructibleLandDoesNotStopDamage() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        harness.addToBattlefield(player1, new HeadwaterSentries());
        harness.addToBattlefield(player2, new HeadwaterSentries());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        harness.assertInGraveyard(player1, "Headwater Sentries");
        harness.assertInGraveyard(player2, "Headwater Sentries");
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDealDamageToPlayers() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not damage non-creature non-planeswalker permanents (e.g. lands)")
    void doesNotDamageOtherPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        // Opponent's island should survive (not a creature or planeswalker)
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Fizzles if target land is removed — no damage dealt")
    void fizzlesIfTargetLandRemoved() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new HeadwaterSentries());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);

        // Remove target land before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        harness.passBothPriorities();

        // Creature should survive because the spell fizzled
        harness.assertOnBattlefield(player2, "Headwater Sentries");
    }

    @Test
    @DisplayName("Star of Extinction goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new StarOfExtinction()));
        harness.addMana(player1, ManaColor.RED, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Star of Extinction");
    }
}
