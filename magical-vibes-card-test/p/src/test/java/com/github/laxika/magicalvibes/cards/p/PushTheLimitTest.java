package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.Aetherjacket;
import com.github.laxika.magicalvibes.cards.b.BrightfieldMustang;
import com.github.laxika.magicalvibes.cards.c.ClamorousIronclad;
import com.github.laxika.magicalvibes.cards.t.ThunderousVelocipede;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PushTheLimit.class, BrightfieldMustang.class, ClamorousIronclad.class, Aetherjacket.class,
        ThunderousVelocipede.class})
class PushTheLimitTest extends BaseCardTest {

    @Test
    @DisplayName("Returns Mounts and Vehicles and sacrifices them at the next end step")
    void returnsMountsAndVehiclesAndSacrificesThemAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new BrightfieldMustang(), new ClamorousIronclad(), new Aetherjacket()));
        harness.setHand(player1, List.of(new PushTheLimit()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Brightfield Mustang");
        harness.assertOnBattlefield(player1, "Clamorous Ironclad");
        harness.assertInGraveyard(player1, "Aetherjacket");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.CLEANUP);

        harness.assertInGraveyard(player1, "Brightfield Mustang");
        harness.assertInGraveyard(player1, "Clamorous Ironclad");
        harness.assertNotOnBattlefield(player1, "Brightfield Mustang");
        harness.assertNotOnBattlefield(player1, "Clamorous Ironclad");
    }

    @Test
    @DisplayName("Makes own Vehicles creatures and gives own creatures haste until end of turn")
    void animatesOwnVehiclesAndGivesOwnCreaturesHasteUntilEndOfTurn() {
        Permanent ownVehicle = harness.addToBattlefieldAndReturn(player1, new ClamorousIronclad());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Aetherjacket());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new ClamorousIronclad());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Aetherjacket());
        harness.setHand(player1, List.of(new PushTheLimit()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentVehicle, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Returned Mounts and Vehicles are immediately animated and have haste")
    void returnedPermanentsAreAnimatedAndHaveHaste() {
        harness.setGraveyard(player1, List.of(new BrightfieldMustang(), new ClamorousIronclad()));
        harness.setGraveyard(player2, List.of(new BrightfieldMustang(), new ClamorousIronclad()));
        harness.setHand(player1, List.of(new PushTheLimit()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent mount = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Brightfield Mustang"));
        Permanent vehicle = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Clamorous Ironclad"));
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.hasKeyword(gd, mount, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Brightfield Mustang");
        harness.assertNotInGraveyard(player1, "Clamorous Ironclad");
        harness.assertInGraveyard(player2, "Brightfield Mustang");
        harness.assertInGraveyard(player2, "Clamorous Ironclad");
        harness.assertNotOnBattlefield(player2, "Brightfield Mustang");
        harness.assertNotOnBattlefield(player2, "Clamorous Ironclad");
    }

    @Test
    @DisplayName("Permanents entering later do not receive the spell's animation or haste")
    void laterPermanentsAreNotAffected() {
        harness.setHand(player1, List.of(new PushTheLimit()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent vehicle = harness.enterBattlefieldAndReturn(player1, new ClamorousIronclad());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new Aetherjacket());

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.CLEANUP);
        harness.assertOnBattlefield(player1, "Clamorous Ironclad");
        harness.assertOnBattlefield(player1, "Aetherjacket");
    }

    @Test
    @DisplayName("One delayed ability sacrifices the entire returned group")
    void sacrificesReturnedGroupWithOneDelayedAbility() {
        harness.setGraveyard(player1, List.of(new BrightfieldMustang(), new ClamorousIronclad()));
        harness.setHand(player1, List.of(new PushTheLimit()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Brightfield Mustang");
        harness.assertOnBattlefield(player1, "Clamorous Ironclad");
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        harness.assertNotOnBattlefield(player1, "Brightfield Mustang");
        harness.assertNotOnBattlefield(player1, "Clamorous Ironclad");
        harness.assertInGraveyard(player1, "Brightfield Mustang");
        harness.assertInGraveyard(player1, "Clamorous Ironclad");
    }

    @Test
    @DisplayName("A returned Velocipede does not add counters to cards entering alongside it")
    void returnedPermanentsEnterSimultaneously() {
        harness.setGraveyard(player1, List.of(new ThunderousVelocipede(), new BrightfieldMustang(),
                new ClamorousIronclad()));
        harness.setHand(player1, List.of(new PushTheLimit()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent mount = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Brightfield Mustang"));
        Permanent vehicle = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Clamorous Ironclad"));
        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Thunderous Velocipede");
    }
}
