package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdrianaCaptainOfTheGuard.class, GrizzlyBears.class})
class AdrianaCaptainOfTheGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Adriana and other creatures you control get one melee boost when they attack")
    void grantsMeleeToOtherCreaturesWithoutAddingAnotherInstanceToAdriana() {
        Permanent adriana = addCreatureReady(player1, new AdrianaCaptainOfTheGuard());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        int adrianaPowerBefore = gqs.getEffectivePower(gd, adriana);
        int adrianaToughnessBefore = gqs.getEffectiveToughness(gd, adriana);
        int otherPowerBefore = gqs.getEffectivePower(gd, otherCreature);
        int otherToughnessBefore = gqs.getEffectiveToughness(gd, otherCreature);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, adriana)).isEqualTo(adrianaPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, adriana)).isEqualTo(adrianaToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(otherPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(otherToughnessBefore + 1);
    }
}
