package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SpringingTiger;
import com.github.laxika.magicalvibes.cards.w.WildMongrel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolleyOfBoulders.class, WildMongrel.class, SpringingTiger.class, AetherBurst.class, Mountain.class})
class VolleyOfBouldersTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage divided among creatures and players")
    void dividesDamageAmongCreaturesAndPlayers() {
        Permanent mongrel = harness.addToBattlefieldAndReturn(player2, new WildMongrel());
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 9);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(mongrel.getId(), 2, player2.getId(), 4));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wild Mongrel");
        harness.assertLife(player2, lifeBefore - 4);
    }

    @Test
    @DisplayName("Assignments must sum to 6 damage")
    void assignmentsMustSumToSix() {
        Permanent tiger = harness.addToBattlefieldAndReturn(player2, new SpringingTiger());
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 9);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(tiger.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each target must receive at least one damage")
    void assignmentsMustBePositive() {
        Permanent mongrel = harness.addToBattlefieldAndReturn(player2, new WildMongrel());
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 9);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                Map.of(mongrel.getId(), 0, player2.getId(), 6)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback repeats the damage and exiles the spell")
    void flashbackDealsDamageAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 6);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashback(player1, 0, Map.of(player2.getId(), 6));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
        harness.assertNotInGraveyard(player1, "Volley of Boulders");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Volley of Boulders"));
    }

    @Test
    @DisplayName("Cannot cast without choosing a target")
    void cannotCastWithoutTargets() {
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 9);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot flash back without choosing a target")
    void cannotFlashbackWithoutTargets() {
        harness.setGraveyard(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land that is not a creature cannot be targeted")
    void cannotTargetNoncreatureLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 9);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(mountain.getId(), 6)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Normal casting accepts eight generic mana and one red mana")
    void normalCastingUsesPrintedCostAndGoesToGraveyard() {
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 6));
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 6);
        harness.assertInGraveyard(player1, "Volley of Boulders");
    }

    @Test
    @DisplayName("Flashback requires six red mana")
    void flashbackCannotUseGenericManaForRedCost() {
        harness.setGraveyard(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, Map.of(player2.getId(), 6)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage assigned to a removed target is not reassigned")
    void removedTargetDoesNotRedistributeDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WildMongrel());
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.setHand(player2, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.RED, 9);
        harness.addMana(player2, ManaColor.BLUE, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(creature.getId(), 2, player2.getId(), 4));
        harness.castAndResolveInstant(player2, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Wild Mongrel");
        harness.assertLife(player2, lifeBefore - 4);
        harness.assertInGraveyard(player1, "Volley of Boulders");
    }

    @Test
    @DisplayName("Flashback exiles the spell even when its only target disappears")
    void flashbackExilesWhenAllTargetsBecomeIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WildMongrel());
        harness.setGraveyard(player1, List.of(new VolleyOfBoulders()));
        harness.setHand(player2, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player2, ManaColor.BLUE, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashback(player1, 0, Map.of(creature.getId(), 6));
        harness.castAndResolveInstant(player2, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Wild Mongrel");
        harness.assertLife(player2, lifeBefore);
        harness.assertNotInGraveyard(player1, "Volley of Boulders");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Volley of Boulders"));
    }

    @Test
    @DisplayName("Allows six targets with one damage assigned to each")
    void allowsSixTargetsIncludingControllerAndOwnCreatures() {
        Permanent ownCreature1 = harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        Permanent ownCreature2 = harness.addToBattlefieldAndReturn(player1, new WildMongrel());
        Permanent opposingCreature1 = harness.addToBattlefieldAndReturn(player2, new WildMongrel());
        Permanent opposingCreature2 = harness.addToBattlefieldAndReturn(player2, new WildMongrel());
        harness.setHand(player1, List.of(new VolleyOfBoulders()));
        harness.addMana(player1, ManaColor.RED, 9);
        int ownLifeBefore = gd.getLife(player1.getId());
        int opposingLifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, Map.of(
                ownCreature1.getId(), 1, ownCreature2.getId(), 1,
                opposingCreature1.getId(), 1, opposingCreature2.getId(), 1,
                player1.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertLife(player1, ownLifeBefore - 1);
        harness.assertLife(player2, opposingLifeBefore - 1);
        assertThat(List.of(ownCreature1, ownCreature2, opposingCreature1, opposingCreature2))
                .allSatisfy(creature -> assertThat(creature.getMarkedDamage()).isEqualTo(1));
        harness.assertInGraveyard(player1, "Volley of Boulders");
    }
}
