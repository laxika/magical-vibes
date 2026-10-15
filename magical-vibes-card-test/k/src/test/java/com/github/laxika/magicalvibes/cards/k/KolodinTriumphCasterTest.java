package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.m.MobilizerMech;
import com.github.laxika.magicalvibes.cards.t.ThunderousVelocipede;
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

@CardUsed({KolodinTriumphCaster.class, BrightfieldGlider.class, ThunderousVelocipede.class,
        MobilizerMech.class})
class KolodinTriumphCasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your Mounts and Vehicles haste")
    void givesOwnMountsAndVehiclesHaste() {
        Permanent ownMount = addCreatureReady(player1, new BrightfieldGlider());
        Permanent ownVehicle = addCreatureReady(player1, new ThunderousVelocipede());
        Permanent opposingMount = addCreatureReady(player2, new BrightfieldGlider());
        addCreatureReady(player1, new KolodinTriumphCaster());

        assertThat(gqs.hasKeyword(gd, ownMount, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingMount, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Saddles a Mount when it enters until end of turn")
    void saddlesEnteringMount() {
        addCreatureReady(player1, new KolodinTriumphCaster());
        BrightfieldGlider mountCard = new BrightfieldGlider();
        harness.setHand(player1, List.of(mountCard));
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mount = findPermanent(player1, "Brightfield Glider");
        assertThat(mount.isSaddled()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Makes an entering Vehicle an artifact creature until end of turn")
    void animatesEnteringVehicle() {
        addCreatureReady(player1, new KolodinTriumphCaster());
        ThunderousVelocipede vehicleCard = new ThunderousVelocipede();
        harness.setHand(player1, List.of(vehicleCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent vehicle = findPermanent(player1, "Thunderous Velocipede");
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.isArtifact(vehicle)).isTrue();
    }

    @Test
    @DisplayName("Haste excludes your non-Mount creatures and opposing Vehicles")
    void hasteExcludesNonMountsAndOpposingVehicles() {
        Permanent opposingVehicle = harness.addToBattlefieldAndReturn(player2, new ThunderousVelocipede());
        Permanent kolodin = harness.addToBattlefieldAndReturn(player1, new KolodinTriumphCaster());

        assertThat(gqs.hasKeyword(gd, opposingVehicle, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, kolodin, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Animating an entering Vehicle does not resolve a crew ability")
    void enteringVehicleDoesNotBecomeCrewed() {
        addCreatureReady(player1, new KolodinTriumphCaster());
        Permanent otherVehicle = harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());
        harness.setHand(player1, List.of(new MobilizerMech()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Mobilizer Mech"))).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, otherVehicle)).isFalse();
    }

    @Test
    @DisplayName("Entry triggers still resolve after Kolodin leaves")
    void entryTriggersSurviveKolodinLeaving() {
        Permanent kolodin = harness.addToBattlefieldAndReturn(player1, new KolodinTriumphCaster());
        Permanent mount = harness.enterBattlefieldAndReturn(player1, new BrightfieldGlider());
        Permanent vehicle = harness.enterBattlefieldAndReturn(player1, new ThunderousVelocipede());
        gd.playerBattlefields.get(player1.getId()).remove(kolodin);

        resolveAllTriggers();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.hasKeyword(gd, mount, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's entering Mounts and Vehicles are unaffected")
    void doesNotTriggerForOpponentsPermanents() {
        harness.addToBattlefield(player1, new KolodinTriumphCaster());
        Permanent mount = harness.enterBattlefieldAndReturn(player2, new BrightfieldGlider());
        Permanent vehicle = harness.enterBattlefieldAndReturn(player2, new ThunderousVelocipede());

        resolveAllTriggers();

        assertThat(mount.isSaddled()).isFalse();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("Kolodin does not saddle or animate permanents already on the battlefield")
    void doesNotAffectPreexistingPermanentsWithEntryTriggers() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());
        harness.enterBattlefieldAndReturn(player1, new KolodinTriumphCaster());

        resolveAllTriggers();

        assertThat(mount.isSaddled()).isFalse();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, mount, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HASTE)).isTrue();
    }
}
