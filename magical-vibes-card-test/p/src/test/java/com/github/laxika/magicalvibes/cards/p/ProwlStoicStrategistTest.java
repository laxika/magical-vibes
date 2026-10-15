package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProwlStoicStrategist.class, ProwlPursuitVehicle.class, GrizzlyBears.class, Mountain.class, BenalishKnight.class, TreetopVillage.class})
class ProwlStoicStrategistTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsProwlConverted() {
        Permanent prowl = castProwlConverted();

        assertThat(prowl.isTransformed()).isTrue();
        assertThat(prowl.getCard()).isInstanceOf(ProwlPursuitVehicle.class);
        assertThat(gqs.isCreature(gd, prowl)).isTrue();
        assertThat(gqs.isArtifact(gd, prowl)).isTrue();
    }

    @Test
    void anotherCreatureEntersPutsCounterAndSecondResolutionTransformsBack() {
        Permanent prowl = castProwlConverted();

        castBear();
        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(prowl.isTransformed()).isTrue();

        castBear();
        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(prowl.isTransformed()).isFalse();
    }

    @Test
    void attackExilesTappedPermanentWithPermissionAndPlayingItDrawsAndTransforms() {
        Permanent prowl = addCreatureReady(player1, new ProwlStoicStrategist());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        ExiledCardEntry exiled = gd.findExiledCard(target.getCard().getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(prowl.getId());
        assertThat(gd.exilePlayPermissions.get(target.getCard().getId())).isEqualTo(player2.getId());

        harness.setLibrary(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, target.getCard().getId());
        harness.passBothPriorities();

        assertThat(prowl.isTransformed()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void livingMetalAppliesOnlyDuringControllersTurn() {
        Permanent prowl = castProwlConverted();
        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceActivePlayer(player2);
        assertThat(gqs.isCreature(gd, prowl)).isFalse();
        assertThat(gqs.isArtifact(gd, prowl)).isTrue();

        harness.forceActivePlayer(player1);
        assertThat(gqs.isCreature(gd, prowl)).isTrue();
    }

    @Test
    void opposingCreaturesAndFriendlyLandsDoNotTriggerCounters() {
        Permanent prowl = castProwlConverted();

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new Mountain());
        resolveAllTriggers();

        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(prowl.isTransformed()).isTrue();
    }

    @Test
    void thirdPendingEnterTriggerStillAddsCounterAfterConversion() {
        Permanent prowl = castProwlConverted();

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(prowl.isTransformed()).isFalse();
    }

    @Test
    void attackRejectsItselfUntappedCreaturesAndTappedNoncreatureLands() {
        Permanent prowl = addCreatureReady(player1, new ProwlStoicStrategist());
        Permanent untapped = addCreatureReady(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        land.tap();
        Permanent legal = addCreatureReady(player2, new GrizzlyBears());
        legal.tap();

        declareAttackers(List.of(0));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, prowl.getId()))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, untapped.getId()))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(RuntimeException.class);
        harness.handlePermanentChosen(player1, legal.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(legal.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(prowl);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(untapped, land);
    }

    @Test
    void attackCanExileTappedNoncreatureVehicle() {
        Permanent vehicle = castProwlConverted();
        vehicle.tap();
        Permanent attacker = addCreatureReady(player2, new ProwlStoicStrategist());

        declareAttackers(player2, List.of(0));
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.handlePermanentChosen(player2, vehicle.getId());
        resolveAllTriggers();

        ExiledCardEntry exiled = gd.findExiledCard(vehicle.getOriginalCard().getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(attacker.getId());
        assertThat(gd.exilePlayPermissions.get(vehicle.getOriginalCard().getId()))
                .isEqualTo(player1.getId());
    }

    @Test
    void playingOwnExiledCreatureConvertsBeforeItEntersAndAddsCounter() {
        Permanent prowl = addCreatureReady(player1, new ProwlStoicStrategist());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bear.getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, bear.getCard().getId());
        resolveAllTriggers();

        assertThat(prowl.isTransformed()).isTrue();
        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void twoPendingPlayedCardTriggersDrawTwiceButConvertOnlyOnce() {
        Permanent prowl = addCreatureReady(player1, new ProwlStoicStrategist());
        BenalishKnight first = new BenalishKnight();
        BenalishKnight second = new BenalishKnight();
        gd.addToExile(player2.getId(), first, prowl.getId());
        gd.addToExile(player2.getId(), second, prowl.getId());
        gd.exilePlayPermissions.put(first.getId(), player2.getId());
        gd.exilePlayPermissions.put(second.getId(), player2.getId());
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castFromExile(player2, first.getId());
        harness.castFromExile(player2, second.getId());
        resolveAllTriggers();

        assertThat(prowl.isTransformed()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card instanceof Mountain)).hasSize(2);
        assertThat(countPermanents(player2, "Benalish Knight")).isEqualTo(2);
    }

    @Test
    void playingExiledAnimatedLandDrawsAndConverts() {
        Permanent prowl = addCreatureReady(player1, new ProwlStoicStrategist());
        Permanent village = harness.addToBattlefieldAndReturn(player2, new TreetopVillage());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
        village.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, village.getId());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, village.getOriginalCard().getId());
        resolveAllTriggers();

        assertThat(prowl.isTransformed()).isTrue();
        harness.assertInHand(player1, "Mountain");
        assertThat(findPermanent(player2, "Treetop Village").isTapped()).isTrue();
    }

    @Test
    void playingCardExiledWithVehicleFaceDoesNotDrawOrConvert() {
        Permanent prowl = castProwlConverted();
        GrizzlyBears bear = new GrizzlyBears();
        gd.addToExile(player2.getId(), bear, prowl.getId());
        gd.exilePlayPermissions.put(bear.getId(), player2.getId());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromExile(player2, bear.getId());
        resolveAllTriggers();

        assertThat(prowl.isTransformed()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(prowl.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent castProwlConverted() {
        harness.setHand(player1, List.of(new ProwlStoicStrategist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        return findPermanent(player1, "Prowl, Pursuit Vehicle");
    }

    private void castBear() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
