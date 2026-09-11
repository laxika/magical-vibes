package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranduilsCompany.class, ElvishMystic.class, Forest.class, GrizzlyBears.class})
class ThranduilsCompanyTest extends BaseCardTest {

    @Test
    @DisplayName("Allows an additional land play while you control another Elf")
    void allowsAdditionalLandWithAnotherElf() {
        addCompany();
        assertThat(gqs.getMaxLandsThisTurn(gd, player1.getId())).isEqualTo(1);

        harness.addToBattlefield(player1, new ElvishMystic());

        assertThat(gqs.getMaxLandsThisTurn(gd, player1.getId())).isEqualTo(2);
        assertThat(gqs.getMaxLandsThisTurn(gd, player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall puts two counters on a target creature and grants vigilance until end of turn")
    void landfallCountersAndVigilanceExpireAtEndOfTurn() {
        addCompany();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new Forest()));

        harness.playLand(player1, 0);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bear.getId()).doesNotContain(opponentBear.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    private void addCompany() {
        harness.addToBattlefield(player1, new ThranduilsCompany());
    }
}
