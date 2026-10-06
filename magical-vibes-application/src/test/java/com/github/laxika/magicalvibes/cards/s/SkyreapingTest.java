package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ArchetypeOfImagination;
import com.github.laxika.magicalvibes.cards.a.ArchetypeOfEndurance;
import com.github.laxika.magicalvibes.cards.c.CourserOfKruphix;
import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skyreaping.class, GrizzlyBears.class, WindDrake.class, SuntailHawk.class,
        ArchetypeOfImagination.class, ArchetypeOfEndurance.class, CourserOfKruphix.class, NyxbornWolf.class})
class SkyreapingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to green devotion to each flying creature")
    void dealsDamageEqualToGreenDevotionToFlyingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WindDrake());
        harness.addToBattlefield(player2, new WindDrake());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSkyreaping();

        harness.assertNotOnBattlefield(player1, "Wind Drake");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts green devotion only among permanents controlled by the caster")
    void countsOnlyCastersGreenDevotion() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        castSkyreaping();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Does not damage creatures without flying")
    void doesNotDamageCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSkyreaping();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Marks nonlethal damage on flyers and no damage on ground creatures")
    void marksNonlethalDamageOnlyOnFlyers() {
        var bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var ownDrake = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        var opposingDrake = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        castSkyreaping();

        harness.assertOnBattlefield(player1, "Wind Drake");
        harness.assertOnBattlefield(player2, "Wind Drake");
        assertThat(ownDrake.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingDrake.getMarkedDamage()).isEqualTo(1);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses devotion at resolution rather than when cast")
    void evaluatesDevotionAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new WindDrake());
        harness.setHand(player1, List.of(new Skyreaping()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wind Drake");
    }

    @Test
    @DisplayName("Damages granted flying creatures simultaneously using their green devotion")
    void countsDyingGreenFlyersAndUsesGrantedFlying() {
        harness.addToBattlefield(player1, new ArchetypeOfImagination());
        harness.addToBattlefield(player1, new NyxbornWolf());
        harness.addToBattlefield(player1, new NyxbornWolf());
        var groundWolf = harness.addToBattlefieldAndReturn(player2, new NyxbornWolf());

        castSkyreaping();

        harness.assertNotOnBattlefield(player1, "Nyxborn Wolf");
        harness.assertNotOnBattlefield(player1, "Archetype of Imagination");
        harness.assertOnBattlefield(player2, "Nyxborn Wolf");
        assertThat(groundWolf.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Counts each green mana symbol and damages opposing hexproof flyers")
    void countsMultipleGreenSymbolsAndIgnoresHexproof() {
        var groundCreature = harness.addToBattlefieldAndReturn(player1, new CourserOfKruphix());
        harness.addToBattlefield(player2, new ArchetypeOfImagination());
        var hexproofFlyer = harness.addToBattlefieldAndReturn(player2, new ArchetypeOfEndurance());

        castSkyreaping();

        harness.assertNotOnBattlefield(player2, "Archetype of Imagination");
        harness.assertOnBattlefield(player2, "Archetype of Endurance");
        assertThat(hexproofFlyer.getMarkedDamage()).isEqualTo(2);
        assertThat(groundCreature.getMarkedDamage()).isZero();
    }

    private void castSkyreaping() {
        harness.setHand(player1, List.of(new Skyreaping()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
    }
}
