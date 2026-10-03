package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrismaticStrands;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreakingPoint.class, DarksteelMyr.class, DrudgeSkeletons.class, Forest.class, GrizzlyBears.class, PrismaticStrands.class, SuntailHawk.class})
class BreakingPointTest extends BaseCardTest {

    @Test
    @DisplayName("A player accepting takes 6 damage and prevents the destruction")
    void acceptingDamagePreventsDestruction() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore - 6);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("All players declining destroys all creatures")
    void allPlayersDecliningDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("All players declining destroys creatures but leaves noncreatures alone")
    void destructionBranchOnlyDestroysCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The first accepting player stops the remaining choices")
    void firstAcceptanceStopsChoices() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, lifeBefore - 6);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures destroyed by the decline branch cannot be regenerated")
    void destructionCannotBeRegenerated() {
        var skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    private void castBreakingPoint() {
        harness.castFromHand(player1, new BreakingPoint(), "{1}{R}{R}");
        harness.passBothPriorities();
    }

    @Test
    @CardUsed({BreakingPoint.class, PrismaticStrands.class, SuntailHawk.class})
    @DisplayName("Accepting prevented damage still prevents creature destruction")
    void acceptingPreventedDamagePreservesCreatures() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setLife(player2, 3);
        harness.castFromHand(player1, new PrismaticStrands(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 3);
        harness.assertOnBattlefield(player1, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Breaking Point");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({BreakingPoint.class})
    @DisplayName("Players may accept damage even when there are no creatures")
    void acceptingDamageWithNoCreatures() {
        int lifeBefore = gd.getLife(player1.getId());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore - 6);
        harness.assertInGraveyard(player1, "Breaking Point");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining destroys only destructible creatures")
    void decliningLeavesNoncreaturesAndIndestructibleCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new DarksteelMyr());

        castBreakingPoint();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Darksteel Myr");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
