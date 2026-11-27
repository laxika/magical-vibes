package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.e.ExaltedAngel;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Maro;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RothgaBondedEngulfer.class, Cancel.class, ExaltedAngel.class, GiantSpider.class, GrizzlyBears.class, Maro.class})
class RothgaBondedEngulferTest extends BaseCardTest {

    @Test
    @DisplayName("Only the next creature spell perpetually gets +X/+X, where X is its power")
    void nextCreatureSpellPerpetuallyGetsPlusPowerPlusPower() {
        RothgaBondedEngulfer rothga = new RothgaBondedEngulfer();
        GiantSpider spider = new GiantSpider();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(rothga, spider, bears));
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent enteredSpider = findPermanent(player1, "Giant Spider");
        Permanent enteredBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, enteredSpider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enteredSpider)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, enteredBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enteredBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("X is the creature spell's power from its characteristic-defining ability")
    void boonUsesCharacteristicDefiningPowerOfCreatureSpell() {
        harness.enterBattlefieldAndReturn(player1, new RothgaBondedEngulfer());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Maro(), new GiantSpider(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent maro = findPermanent(player1, "Maro");
        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's creature spell neither gets the boost nor uses the boon")
    void opponentsCreatureSpellDoesNotConsumeBoon() {
        harness.enterBattlefieldAndReturn(player1, new RothgaBondedEngulfer());
        resolveAllTriggers();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        Permanent opposingBears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent spider = findPermanent(player1, "Giant Spider");
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature spell countered before the boon's ability resolves is not boosted (CR 400.7)")
    void counteredCreatureSpellIsNotBoosted() {
        harness.enterBattlefieldAndReturn(player1, new RothgaBondedEngulfer());
        resolveAllTriggers();
        GiantSpider spider = new GiantSpider();
        harness.setHand(player1, List.of(spider));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spider.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Giant Spider");
        assertThat(gd.perpetualCardPowerToughnessModifiers).doesNotContainKey(spider.getId());
    }

    @Test
    @DisplayName("X is a face-down creature spell's power of 2, not its real card's")
    void faceDownCreatureSpellUsesItsFaceDownPower() {
        harness.enterBattlefieldAndReturn(player1, new RothgaBondedEngulfer());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new ExaltedAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, 1);

        Permanent angel = findPermanent(player1, "Exalted Angel");
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(7);
    }
}
