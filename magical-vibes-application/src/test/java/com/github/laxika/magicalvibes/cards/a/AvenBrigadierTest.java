package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GustcloakHarrier;
import com.github.laxika.magicalvibes.cards.g.GustcloakRunner;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenBrigadier.class, AvenSoulgazer.class, GustcloakRunner.class,
        GustcloakHarrier.class, ElvishWarrior.class})
class AvenBrigadierTest extends BaseCardTest {

    @Test
    @DisplayName("Birds and Soldiers get +1/+1, and creatures with both types get +2/+2")
    void buffsBirdsAndSoldiers() {
        Permanent bird = harness.addToBattlefieldAndReturn(player1, new AvenSoulgazer());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new GustcloakRunner());
        Permanent birdAndSoldier = harness.addToBattlefieldAndReturn(player1, new GustcloakHarrier());
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
        Permanent brigadier = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.getEffectivePower(gd, brigadier)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, brigadier)).isEqualTo(10);
    }

    @Test
    @DisplayName("Aven Brigadier buffs Birds and Soldiers controlled by an opponent")
    void buffsOpponentsBirdsAndSoldiers() {
        Permanent bird = harness.addToBattlefieldAndReturn(player2, new AvenSoulgazer());
        Permanent soldier = harness.addToBattlefieldAndReturn(player2, new GustcloakRunner());
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
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        int basePower = gqs.getEffectivePower(gd, otherCreature);
        int baseToughness = gqs.getEffectiveToughness(gd, otherCreature);

        harness.addToBattlefield(player1, new AvenBrigadier());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Two Aven Brigadiers buff each other for both creature types")
    void twoBrigadiersBuffEachOther() {
        AvenBrigadier firstCard = new AvenBrigadier();
        firstCard.setPower(10);
        firstCard.setToughness(10);
        AvenBrigadier secondCard = new AvenBrigadier();
        secondCard.setPower(10);
        secondCard.setToughness(10);

        Permanent first = harness.addToBattlefieldAndReturn(player1, firstCard);
        Permanent second = harness.addToBattlefieldAndReturn(player1, secondCard);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(12);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(12);
    }
}
