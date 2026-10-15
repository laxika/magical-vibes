package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.RecklessCohort;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SokkasCharge.class, RecklessCohort.class, GrizzlyBears.class,
        Opalescence.class, MaskwoodNexus.class})
class SokkasChargeTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, your Allies have double strike and lifelink")
    void grantsKeywordsToAlliesDuringYourTurn() {
        harness.addToBattlefield(player1, new SokkasCharge());
        Permanent ownAlly = harness.addToBattlefieldAndReturn(player1, new RecklessCohort());
        Permanent ownNonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new RecklessCohort());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonAlly, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownNonAlly, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingAlly, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingAlly, Keyword.LIFELINK)).isFalse();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownAlly, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The keyword grant begins when the enchantment resolves")
    void grantsKeywordsOnlyAfterResolution() {
        harness.forceActivePlayer(player1);
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new RecklessCohort());

        harness.castFromHand(player1, new SokkasCharge(), "{3}{W}");

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("New Allies immediately gain the keywords and lose them when the source leaves")
    void grantUpdatesForNewAlliesAndSourceRemoval() {
        harness.forceActivePlayer(player1);
        Permanent charge = harness.addToBattlefieldAndReturn(player1, new SokkasCharge());
        Permanent ally = harness.enterBattlefieldAndReturn(player1, new RecklessCohort());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, charge));

        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Sokka's Charge grants itself the keywords when it becomes an Ally creature")
    @CardUsed({SokkasCharge.class, Opalescence.class, MaskwoodNexus.class})
    void grantsKeywordsToItselfWhenAnimatedAsAnAlly() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent charge = harness.addToBattlefieldAndReturn(player1, new SokkasCharge());

        assertThat(gqs.hasKeyword(gd, charge, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, charge, Keyword.LIFELINK)).isTrue();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, charge, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, charge, Keyword.LIFELINK)).isFalse();
    }
}
