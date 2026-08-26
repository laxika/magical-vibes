package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfSingularity.class, GrizzlyBears.class, Forest.class})
class LeylineOfSingularityTest extends BaseCardTest {

    @Test
    @DisplayName("All nonland permanents become legendary")
    void allNonlandPermanentsBecomeLegendary() {
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfSingularity());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.hasEffectiveSupertype(gd, leyline, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, ownCreature, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, opposingCreature, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.LEGENDARY)).isFalse();
    }

    @Test
    @DisplayName("The legend rule sees nonland permanents made legendary by Leyline of Singularity")
    void legendRuleSeesGrantedLegendarySupertype() {
        harness.addToBattlefield(player1, new LeylineOfSingularity());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }

    @Test
    @DisplayName("Nonland permanents stop being legendary when Leyline of Singularity leaves")
    void effectEndsWhenLeylineLeaves() {
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfSingularity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        gd.playerBattlefields.get(player1.getId()).remove(leyline);

        assertThat(gqs.hasEffectiveSupertype(gd, creature, CardSupertype.LEGENDARY)).isFalse();
    }
}
