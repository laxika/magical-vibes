package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitaniaProudPummeler.class, GrizzlyBears.class})
class TitaniaProudPummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have melee")
    void grantsMeleeToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new TitaniaProudPummeler());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MELEE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MELEE)).isFalse();
    }

    @Test
    @DisplayName("Melee gives an attacking creature +1/+1 until end of turn")
    void meleeBoostsAttackingCreature() {
        harness.addToBattlefield(player1, new TitaniaProudPummeler());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, attacker);
        int toughnessBefore = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughnessBefore + 1);
    }
}
