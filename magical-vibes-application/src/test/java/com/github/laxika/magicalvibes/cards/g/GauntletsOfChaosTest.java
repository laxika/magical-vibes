package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AladdinsRing;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LivingLands;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GauntletsOfChaos.class, AladdinsRing.class, GrizzlyBears.class, LlanowarElves.class,
        HolyStrength.class, Forest.class, Plains.class, LivingLands.class})
class GauntletsOfChaosTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of the two creatures")
    void exchangesControlOfCreatures() {
        harness.addToBattlefield(player1, new GauntletsOfChaos());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opp.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        // The artifact was sacrificed as part of the cost.
        harness.assertNotOnBattlefield(player1, "Gauntlets of Chaos");
    }

    @Test
    @DisplayName("Destroys Auras attached to both exchanged permanents")
    void destroysAurasAttachedToExchangedPermanents() {
        harness.addToBattlefield(player1, new GauntletsOfChaos());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        ownAura.setAttachedTo(own.getId());
        Permanent oppAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        oppAura.setAttachedTo(opp.getId());

        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opp.getId()));
        harness.passBothPriorities();

        // Control still swapped, and both Auras are destroyed.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player2, "Holy Strength");
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertInGraveyard(player2, "Holy Strength");
    }

    @Test
    @DisplayName("Exchanges control of two artifacts")
    void exchangesControlOfArtifacts() {
        harness.addToBattlefield(player1, new GauntletsOfChaos());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AladdinsRing());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new AladdinsRing());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opp.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opp);
    }

    @Test
    @DisplayName("Exchanges control of two lands (shared land type)")
    void exchangesControlOfLands() {
        harness.addToBattlefield(player1, new GauntletsOfChaos());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opp.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Rejects a pair that shares none of the three types")
    void rejectsTargetsSharingNoType() {
        harness.addToBattlefield(player1, new GauntletsOfChaos());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // creature
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new Forest());       // land
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opp.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Offers only a second target that shares an artifact, creature, or land type")
    void offersOnlySecondTargetsSharingAListedType() {
        Permanent gauntlets = harness.addToBattlefieldAndReturn(player1, new GauntletsOfChaos());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent matching = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent nonMatching = harness.addToBattlefieldAndReturn(player2, new Forest());

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, gauntlets.getCard(), gauntlets.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), 0, List.of(own.getId()));

        assertThat(response.validPermanentIds())
                .containsExactly(matching.getId())
                .doesNotContain(nonMatching.getId());
    }

    @Test
    @DisplayName("Uses a type granted by a continuous effect when checking shared types")
    void exchangesTargetsThatShareAnEffectivelyGrantedType() {
        harness.addToBattlefield(player1, new GauntletsOfChaos());
        harness.addToBattlefield(player1, new LivingLands());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opp.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opp);
    }

    @Test
    @DisplayName("Exchange fizzles when a target leaves before resolution")
    void exchangeFizzlesWhenTargetLeaves() {
        harness.addToBattlefield(player1, new GauntletsOfChaos());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opp.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opp);
        harness.passBothPriorities();

        // No exchange happened — the controller keeps their creature.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
