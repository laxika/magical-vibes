package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrashTheParty.class, GrizzlyBears.class, Mountain.class})
class CrashThePartyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a tapped Rhino Warrior for each tapped creature you control")
    void createsTappedRhinoWarriorsForTappedCreatures() {
        Permanent firstTappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstTappedCreature.tap();
        Permanent secondTappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        secondTappedCreature.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castFromHand(player1, new CrashTheParty(), "{5}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(2)
                .allSatisfy(rhino -> {
                    assertThat(rhino.isTapped()).isTrue();
                    assertThat(rhino.getEffectivePower()).isEqualTo(4);
                    assertThat(rhino.getEffectiveToughness()).isEqualTo(4);
                });
    }

    @Test
    @DisplayName("Creates no tokens when you control no tapped creatures")
    void createsNoTokensWithoutTappedCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentTappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentTappedCreature.tap();

        harness.castFromHand(player1, new CrashTheParty(), "{5}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino Warrior")).isEmpty();
    }

    @Test
    void countsCreaturesTappedAfterCasting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new CrashTheParty(), "{5}{G}");
        creature.tap();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1)
                .allSatisfy(rhino -> assertThat(rhino.isTapped()).isTrue());
    }

    @Test
    void excludesCreaturesUntappedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        harness.castFromHand(player1, new CrashTheParty(), "{5}{G}");
        creature.untap();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino Warrior")).isEmpty();
    }

    @Test
    void excludesTappedNoncreaturesAndOpponentsCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        land.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingCreature.tap();

        harness.castFromHand(player1, new CrashTheParty(), "{5}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
        assertThat(findPermanents(player2, "Rhino Warrior")).isEmpty();
    }

    @Test
    void countsExistingTappedTokensWithoutCountingTokensBeingCreated() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.castFromHand(player1, new CrashTheParty(), "{5}{G}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);

        harness.castFromHand(player1, new CrashTheParty(), "{5}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(3)
                .allSatisfy(rhino -> assertThat(rhino.isTapped()).isTrue());
    }
}
