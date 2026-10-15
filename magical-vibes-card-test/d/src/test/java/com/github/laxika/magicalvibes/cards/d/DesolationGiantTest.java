package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BattlefieldForge;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DesolationGiant.class, BattlefieldForge.class, GaeasSkyfolk.class})
class DesolationGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, destroys other creatures I control and survives")
    void withoutKickerDestroysOtherCreaturesIControl() {
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        harness.addToBattlefield(player2, new GaeasSkyfolk());
        harness.castFromHand(player1, new DesolationGiant(), "{2}{R}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Desolation Giant");
        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");
        harness.assertOnBattlefield(player2, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("When kicked, destroys other creatures on both battlefields and survives")
    void whenKickedDestroysOtherCreaturesOnBothBattlefields() {
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        harness.addToBattlefield(player2, new GaeasSkyfolk());
        harness.setHand(player1, List.of(new DesolationGiant()));
        addBaseMana();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Desolation Giant");
        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");
        harness.assertNotOnBattlefield(player2, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("Without kicker, leaves noncreature permanents alone")
    void withoutKickerLeavesNoncreaturePermanentsAlone() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        harness.addToBattlefield(player2, new BattlefieldForge());
        harness.castFromHand(player1, new DesolationGiant(), "{2}{R}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Battlefield Forge");
        harness.assertOnBattlefield(player2, "Battlefield Forge");
    }

    @Test
    @DisplayName("When kicked, leaves noncreature permanents alone")
    void whenKickedLeavesNoncreaturePermanentsAlone() {
        harness.addToBattlefield(player1, new BattlefieldForge());
        harness.addToBattlefield(player2, new BattlefieldForge());
        harness.setHand(player1, List.of(new DesolationGiant()));
        addBaseMana();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Battlefield Forge");
        harness.assertOnBattlefield(player2, "Battlefield Forge");
    }

    @Test
    @DisplayName("Without kicker, destroys another Desolation Giant but not itself")
    void withoutKickerDestroysAnotherGiant() {
        harness.addToBattlefield(player1, new DesolationGiant());
        harness.castFromHand(player1, new DesolationGiant(), "{2}{R}{R}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Desolation Giant");
        harness.assertOnBattlefield(player1, "Desolation Giant");
    }

    @Test
    @DisplayName("When kicked, destroys another Desolation Giant but not itself")
    void whenKickedDestroysAnotherGiant() {
        harness.addToBattlefield(player2, new DesolationGiant());
        harness.setHand(player1, List.of(new DesolationGiant()));
        addBaseMana();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Desolation Giant");
        harness.assertNotOnBattlefield(player2, "Desolation Giant");
        harness.assertOnBattlefield(player1, "Desolation Giant");
    }

    @Test
    @DisplayName("Destroys creatures present when the enter trigger resolves")
    void destroysCreatureAddedAfterTriggerWasQueued() {
        harness.castFromHand(player1, new DesolationGiant(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        harness.addToBattlefield(player2, new GaeasSkyfolk());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gaea's Skyfolk");
        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");
        harness.assertOnBattlefield(player2, "Gaea's Skyfolk");
        harness.assertOnBattlefield(player1, "Desolation Giant");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
