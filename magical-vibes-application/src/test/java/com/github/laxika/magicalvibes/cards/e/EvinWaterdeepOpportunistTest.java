package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AshnodsAltar;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvinWaterdeepOpportunist.class, DiabolicEdict.class, GrizzlyBears.class,
        Treasure.class, AshnodsAltar.class, LightningBolt.class, MishrasFactory.class})
class EvinWaterdeepOpportunistTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 for each Treasure you control")
    void scalesPowerWithTreasures() {
        Permanent evin = harness.addToBattlefieldAndReturn(player1, new EvinWaterdeepOpportunist());
        harness.addToBattlefield(player1, new Treasure());
        harness.addToBattlefield(player1, new Treasure());

        assertThat(gqs.getEffectivePower(gd, evin)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, evin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creates one tapped Treasure when a creature is sacrificed, once each turn")
    void createsTappedTreasureOnceEachTurn() {
        harness.addToBattlefield(player1, new EvinWaterdeepOpportunist());
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AshnodsAltar());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(altar), null, null);
        harness.handlePermanentChosen(player1, firstBear.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(altar), null, null);
        harness.handlePermanentChosen(player1, secondBear.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void opponentsTreasuresDoNotBoostEvin() {
        Permanent evin = harness.addToBattlefieldAndReturn(player1, new EvinWaterdeepOpportunist());
        harness.addToBattlefield(player2, new Treasure());

        assertThat(gqs.getEffectivePower(gd, evin)).isEqualTo(2);
    }

    @Test
    void sacrificingEvinItselfCreatesTreasure() {
        Permanent evin = harness.addToBattlefieldAndReturn(player1, new EvinWaterdeepOpportunist());
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AshnodsAltar());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(altar), null, null);
        harness.handlePermanentChosen(player1, evin.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Evin, Waterdeep Opportunist");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();
    }

    @Test
    void sacrificingAnimatedLandToEdictCreatesTreasure() {
        harness.addToBattlefield(player1, new EvinWaterdeepOpportunist());
        Permanent factory = harness.addToBattlefieldAndReturn(player2, new MishrasFactory());
        prepareMainPhase(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(factory), 0, null, null);
        resolveAllTriggers();
        harness.setHand(player2, List.of(new DiabolicEdict()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Mishra's Factory");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();
    }

    @Test
    void payingWardSacrificesOpponentsCreatureAndCreatesTreasure() {
        Permanent evin = harness.addToBattlefieldAndReturn(player1, new EvinWaterdeepOpportunist());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, evin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, bear.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(evin.getMarkedDamage()).isEqualTo(3);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();
    }

    @Test
    void decliningWardCountersSpellWithoutCreatingTreasure() {
        Permanent evin = harness.addToBattlefieldAndReturn(player1, new EvinWaterdeepOpportunist());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, evin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(evin.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void sacrificeTriggerResetsOnOpponentsTurn() {
        harness.addToBattlefield(player1, new EvinWaterdeepOpportunist());
        Permanent ownAltar = harness.addToBattlefieldAndReturn(player1, new AshnodsAltar());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingAltar = harness.addToBattlefieldAndReturn(player2, new AshnodsAltar());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ownAltar), null, null);
        harness.handlePermanentChosen(player1, ownBear.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opposingAltar), null, null);
        harness.handlePermanentChosen(player2, opposingBear.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent evin = harness.addToBattlefieldAndReturn(player1, new EvinWaterdeepOpportunist());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, evin.getId());

        assertThat(evin.getMarkedDamage()).isEqualTo(3);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
