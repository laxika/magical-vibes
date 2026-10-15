package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurasaPyromancer.class, RiverBoa.class, StoneworkPuma.class, Conspiracy.class, IntoTheRoil.class})
class MurasaPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may deal damage equal to the Ally count")
    void ownAllyEntryMayDealDamageForEachAlly() {
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.addToBattlefield(player2, new RiverBoa());
        harness.castFromHand(player1, new MurasaPyromancer(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, findPermanent(player2, "River Boa").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "River Boa");
    }

    @Test
    @DisplayName("Another Ally entering triggers Murasa Pyromancer")
    void anotherAllyEntryTriggers() {
        harness.addToBattlefield(player1, new MurasaPyromancer());
        harness.addToBattlefield(player2, new RiverBoa());
        harness.castFromHand(player1, new StoneworkPuma(), "{3}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, findPermanent(player2, "River Boa").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "River Boa");
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger Murasa Pyromancer")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new MurasaPyromancer());
        harness.addToBattlefield(player2, new RiverBoa());
        harness.castFromHand(player1, new RiverBoa(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "River Boa");
    }

    @Test
    @DisplayName("Declining the may ability prevents the damage")
    void mayBeDeclined() {
        harness.addToBattlefield(player2, new RiverBoa());
        harness.castFromHand(player1, new MurasaPyromancer(), "{4}{R}{R}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, findPermanent(player2, "River Boa").getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "River Boa");
    }

    @Test
    @DisplayName("An opponent's Ally entering does not trigger your Pyromancer")
    void opponentsAllyDoesNotTrigger() {
        harness.addToBattlefield(player1, new MurasaPyromancer());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new StoneworkPuma(), "{3}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player2, "Stonework Puma").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The ability can target a creature you control")
    void canDamageOwnCreature() {
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.castFromHand(player1, new MurasaPyromancer(), "{4}{R}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Stonework Puma"));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Stonework Puma");
        harness.assertOnBattlefield(player1, "Murasa Pyromancer");
    }

    @Test
    @DisplayName("Opponent's Allies are not included in the damage count")
    void onlyCountsControllersAllies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.addToBattlefield(player2, new StoneworkPuma());
        harness.castFromHand(player1, new MurasaPyromancer(), "{4}{R}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Damage counts Allies at resolution after the source leaves")
    void countsAlliesAtResolutionAfterSourceLeaves() {
        harness.addToBattlefield(player1, new StoneworkPuma());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.castFromHand(player1, new MurasaPyromancer(), "{4}{R}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Murasa Pyromancer"));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Murasa Pyromancer");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Its own entry triggers even when Conspiracy replaces its creature types")
    void ownEntryTriggersWithoutAllySubtype() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.castFromHand(player1, new MurasaPyromancer(), "{4}{R}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not resolve after its only target leaves")
    void targetLeavingPreventsResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.castFromHand(player1, new MurasaPyromancer(), "{4}{R}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player2, "Stonework Puma");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getMarkedDamage()).isZero();
    }
}
