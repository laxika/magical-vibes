package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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
    @DisplayName("Other creatures you control have melee")
    void grantsMeleeToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new AdrianaCaptainOfTheGuard());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MELEE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MELEE)).isFalse();
    }

    @Test
    @DisplayName("Melee gives an attacking creature +1/+1 until end of turn")
    void meleeBoostsAttackingCreature() {
        harness.addToBattlefield(player1, new AdrianaCaptainOfTheGuard());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, attacker);
        int toughnessBefore = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Adriana does not gain a second melee boost from her own ability")
    void doesNotAddAnotherMeleeInstanceToAdriana() {
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
