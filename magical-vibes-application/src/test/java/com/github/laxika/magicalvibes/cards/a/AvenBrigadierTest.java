package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BirdMaiden;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenBrigadier.class, AvenSquire.class, BirdMaiden.class, EliteVanguard.class, GrizzlyBears.class})
class AvenBrigadierTest extends BaseCardTest {

    @Test
    @DisplayName("Birds and Soldiers get +1/+1, and creatures with both types get +2/+2")
    void buffsBirdsAndSoldiers() {
        harness.addToBattlefield(player1, new BirdMaiden());
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player1, new AvenSquire());
        Permanent bird = findPermanent(player1, "Bird Maiden");
        Permanent soldier = findPermanent(player1, "Elite Vanguard");
        Permanent birdAndSoldier = findPermanent(player1, "Aven Squire");
        int birdBasePower = gqs.getEffectivePower(gd, bird);
        int birdBaseToughness = gqs.getEffectiveToughness(gd, bird);
        int soldierBasePower = gqs.getEffectivePower(gd, soldier);
        int soldierBaseToughness = gqs.getEffectiveToughness(gd, soldier);
        int birdAndSoldierBasePower = gqs.getEffectivePower(gd, birdAndSoldier);
        int birdAndSoldierBaseToughness = gqs.getEffectiveToughness(gd, birdAndSoldier);
        harness.addToBattlefield(player1, new AvenBrigadier());

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(birdBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(birdBaseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierBaseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, birdAndSoldier)).isEqualTo(birdAndSoldierBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, birdAndSoldier)).isEqualTo(birdAndSoldierBaseToughness + 2);
    }

    @Test
    @DisplayName("Aven Brigadier does not buff itself")
    void doesNotBuffItself() {
        AvenBrigadier card = new AvenBrigadier();
        card.setPower(10);
        card.setToughness(10);
        harness.addToBattlefield(player1, card);

        Permanent brigadier = findPermanent(player1, "Aven Brigadier");

        assertThat(gqs.getEffectivePower(gd, brigadier)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, brigadier)).isEqualTo(10);
    }

    @Test
    @DisplayName("Aven Brigadier buffs Birds and Soldiers controlled by an opponent")
    void buffsOpponentsBirdsAndSoldiers() {
        harness.addToBattlefield(player2, new BirdMaiden());
        harness.addToBattlefield(player2, new EliteVanguard());
        Permanent bird = findPermanent(player2, "Bird Maiden");
        Permanent soldier = findPermanent(player2, "Elite Vanguard");
        int birdBasePower = gqs.getEffectivePower(gd, bird);
        int birdBaseToughness = gqs.getEffectiveToughness(gd, bird);
        int soldierBasePower = gqs.getEffectivePower(gd, soldier);
        int soldierBaseToughness = gqs.getEffectiveToughness(gd, soldier);

        harness.addToBattlefield(player1, new AvenBrigadier());

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(birdBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(birdBaseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierBaseToughness + 1);
    }

    @Test
    @DisplayName("Aven Brigadier does not buff creatures without either type")
    void doesNotBuffOtherCreatureTypes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.addToBattlefield(player1, new AvenBrigadier());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
    }
}
