package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarCleansing.class, GrizzlyBears.class, HonorOfThePure.class, PalladiumMyr.class,
        Plains.class, GarrukWildspeaker.class, DarksteelColossus.class, DrudgeSkeletons.class,
        RuneclawBear.class})
class PlanarCleansingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures on both sides")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys enchantments")
    void destroysEnchantments() {
        harness.addToBattlefield(player1, new HonorOfThePure());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Honor of the Pure");

        harness.assertInGraveyard(player1, "Honor of the Pure");
    }

    @Test
    @DisplayName("Destroys artifacts")
    void destroysArtifacts() {
        harness.addToBattlefield(player2, new PalladiumMyr());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Palladium Myr");

        harness.assertInGraveyard(player2, "Palladium Myr");
    }

    @Test
    @DisplayName("Does not destroy lands")
    void doesNotDestroyLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Land should survive
        harness.assertOnBattlefield(player1, "Plains");

        // Creature should be destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Planar Cleansing goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Planar Cleansing");
    }

    @Test
    @DisplayName("Destroys planeswalkers on both sides")
    void destroysPlaneswalkers() {
        harness.addToBattlefield(player1, new GarrukWildspeaker());
        harness.addToBattlefield(player2, new GarrukWildspeaker());

        harness.castFromHand(player1, new PlanarCleansing(), "{3}{W}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Garruk Wildspeaker");
        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player1, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Indestructible permanents survive while other nonland permanents are destroyed")
    void indestructibleSurvives() {
        harness.addToBattlefield(player2, new DarksteelColossus());
        harness.addToBattlefield(player2, new RuneclawBear());

        harness.castFromHand(player1, new PlanarCleansing(), "{3}{W}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Colossus");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Regeneration replaces destruction")
    void regenerationSavesCreature() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new PlanarCleansing(), "{3}{W}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        assertThat(skeletons.isTapped()).isTrue();
        assertThat(skeletons.getRegenerationShield()).isZero();
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }
}
