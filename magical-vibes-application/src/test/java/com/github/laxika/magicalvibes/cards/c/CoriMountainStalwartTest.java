package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.Lifelink;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoriMountainStalwart.class, LightningBolt.class, Lifelink.class})
class CoriMountainStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Flurry deals 2 damage to each opponent and gains 2 life on the second spell only")
    void flurryDealsDamageAndGainsLifeOnSecondSpellOnly() {
        addCreatureReady(player1, new CoriMountainStalwart());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Casting Stalwart as the first spell counts toward flurry after it enters")
    void countsSpellCastBeforeEnteringBattlefield() {
        harness.setHand(player1, List.of(new CoriMountainStalwart(), new CoriMountainStalwart()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent casting their second spell does not trigger flurry")
    void opponentsSpellsDoNotTriggerFlurry() {
        addCreatureReady(player1, new CoriMountainStalwart());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Flurry resolves before the second spell and survives removal of Stalwart")
    void triggeredAbilitySurvivesSourceRemoval() {
        var stalwart = addCreatureReady(player1, new CoriMountainStalwart());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, stalwart.getId());
        harness.assertNotOnBattlefield(player1, "Cori Mountain Stalwart");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 15);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Flurry damage gains additional life when Stalwart has lifelink")
    void flurryDamageUsesGrantedLifelink() {
        var stalwart = addCreatureReady(player1, new CoriMountainStalwart());
        harness.setHand(player1, List.of(new Lifelink(), new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, stalwart.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 15);
    }
}
