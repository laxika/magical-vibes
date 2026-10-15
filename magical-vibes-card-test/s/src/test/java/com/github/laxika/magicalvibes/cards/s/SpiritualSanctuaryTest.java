package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritualSanctuary.class, Plains.class, ZuranOrb.class})
class SpiritualSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("The active player gains 1 life when they control a Plains")
    void activePlayerGainsLifeWithPlains() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The ability does nothing when the active player controls no Plains")
    void doesNotGainLifeWithoutPlains() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The ability does nothing if the Plains leaves before the trigger resolves")
    void doesNotGainLifeWhenPlainsLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.setLife(player1, 18);

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 2, 0, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Plains");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("On the opponent's upkeep, the opponent gains the life")
    void opponentGainsLifeOnTheirUpkeep() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.addToBattlefield(player2, new Plains());
        harness.setLife(player2, 19);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Multiple Plains still cause only one life to be gained")
    void multiplePlainsGainOnlyOneLife() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player1, 17);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each Sanctuary triggers independently for the active player")
    void multipleSanctuariesEachGainLife() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.addToBattlefield(player2, new SpiritualSanctuary());
        harness.addToBattlefield(player2, new Plains());
        harness.setLife(player2, 17);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The controller's Plains does not qualify the opponent for life gain")
    void onlyActivePlayersPlainsQualifies() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player2, 17);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A Plains entering after upkeep begins cannot create a missed trigger")
    void plainsEnteringAfterUpkeepDoesNotGainLife() {
        harness.addToBattlefield(player1, new SpiritualSanctuary());
        harness.setLife(player1, 17);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
    }
}
