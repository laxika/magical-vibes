package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.cards.h.HypnoticGrifter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TakeToTheStreets.class, CivicGardener.class, HypnoticGrifter.class})
class TakeToTheStreetsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all your creatures, with an additional boost and vigilance for Citizens")
    void boostsCreaturesAndCitizens() {
        Permanent ownCreature = addCreatureReady(player1, new HypnoticGrifter());
        Permanent ownCitizen = addCreatureReady(player1, new CivicGardener());
        Permanent opponentCitizen = addCreatureReady(player2, new CivicGardener());
        int ownCreaturePower = gqs.getEffectivePower(gd, ownCreature);
        int ownCreatureToughness = gqs.getEffectiveToughness(gd, ownCreature);
        int ownCitizenPower = gqs.getEffectivePower(gd, ownCitizen);
        int ownCitizenToughness = gqs.getEffectiveToughness(gd, ownCitizen);
        int opponentCitizenPower = gqs.getEffectivePower(gd, opponentCitizen);

        castTakeToTheStreets();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(ownCreaturePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(ownCreatureToughness + 2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCitizen)).isEqualTo(ownCitizenPower + 3);
        assertThat(gqs.getEffectiveToughness(gd, ownCitizen)).isEqualTo(ownCitizenToughness + 3);
        assertThat(gqs.hasKeyword(gd, ownCitizen, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCitizen)).isEqualTo(opponentCitizenPower);
        assertThat(gqs.hasKeyword(gd, opponentCitizen, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The temporary boosts and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new HypnoticGrifter());
        Permanent ownCitizen = addCreatureReady(player1, new CivicGardener());
        int ownCreaturePower = gqs.getEffectivePower(gd, ownCreature);
        int ownCitizenPower = gqs.getEffectivePower(gd, ownCitizen);

        castTakeToTheStreets();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(ownCreaturePower);
        assertThat(gqs.getEffectivePower(gd, ownCitizen)).isEqualTo(ownCitizenPower);
        assertThat(gqs.hasKeyword(gd, ownCitizen, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither boost nor vigilance")
    void laterCreaturesAreNotAffected() {
        castTakeToTheStreets();

        Permanent creature = addCreatureReady(player1, new HypnoticGrifter());
        Permanent citizen = addCreatureReady(player1, new CivicGardener());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, citizen, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Repeated casts give cumulative boosts to existing creatures")
    void repeatedCastsStack() {
        Permanent creature = addCreatureReady(player1, new HypnoticGrifter());
        Permanent citizen = addCreatureReady(player1, new CivicGardener());
        int creaturePower = gqs.getEffectivePower(gd, creature);
        int creatureToughness = gqs.getEffectiveToughness(gd, creature);
        int citizenPower = gqs.getEffectivePower(gd, citizen);
        int citizenToughness = gqs.getEffectiveToughness(gd, citizen);

        castTakeToTheStreets();
        castTakeToTheStreets();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(creaturePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(creatureToughness + 4);
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(citizenPower + 6);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(citizenToughness + 6);
        assertThat(gqs.hasKeyword(gd, citizen, Keyword.VIGILANCE)).isTrue();
    }

    private void castTakeToTheStreets() {
        harness.setHand(player1, List.of(new TakeToTheStreets()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
    }
}
