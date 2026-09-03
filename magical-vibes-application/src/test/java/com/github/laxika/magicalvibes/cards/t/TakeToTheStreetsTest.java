package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GallantCitizen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TakeToTheStreets.class, GallantCitizen.class, GrizzlyBears.class})
class TakeToTheStreetsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all your creatures, with an additional boost and vigilance for Citizens")
    void boostsCreaturesAndCitizens() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownCitizen = addCreatureReady(player1, new GallantCitizen());
        Permanent opponentCitizen = addCreatureReady(player2, new GallantCitizen());
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
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownCitizen = addCreatureReady(player1, new GallantCitizen());
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

    private void castTakeToTheStreets() {
        harness.setHand(player1, List.of(new TakeToTheStreets()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
