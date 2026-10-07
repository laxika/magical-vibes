package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitfireLagac.class, Forest.class, AshayaSoulOfTheWild.class, IntoTheRoil.class})
class SpitfireLagacTest extends BaseCardTest {

    @Test
    @DisplayName("Your land entering deals 1 damage to each opponent")
    void ownLandDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new SpitfireLagac());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent's land entering does not trigger Spitfire Lagac")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpitfireLagac());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new SpitfireLagac());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void eachCopyTriggersForTheSameLand() {
        harness.addToBattlefield(player1, new SpitfireLagac());
        harness.addToBattlefield(player1, new SpitfireLagac());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void triggerResolvesAfterSourceReturnsToHand() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SpitfireLagac());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.playLand(player1, 0);
        harness.castAndResolveInstant(player2, 0, source.getId());

        harness.assertInHand(player1, "Spitfire Lagac");
        harness.assertNotOnBattlefield(player1, "Spitfire Lagac");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void enteringWithoutBeingALandDoesNotTriggerItself() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new SpitfireLagac(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void triggersForItsOwnEntryWhenAshayaMakesItALand() {
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new SpitfireLagac(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    void ashayaEnteringTriggersOnceWithoutRetriggeringExistingCreatures() {
        harness.addToBattlefield(player1, new SpitfireLagac());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new AshayaSoulOfTheWild(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
