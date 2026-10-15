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

    @Test
    @DisplayName("An opponent's Elf does not enable the additional land play")
    void opposingElfDoesNotEnableAdditionalLand() {
        addCompany();
        harness.addToBattlefield(player2, new ThranduilsCompany());

        assertThat(gqs.getMaxLandsThisTurn(gd, player1.getId())).isEqualTo(1);
        assertThat(gqs.getMaxLandsThisTurn(gd, player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Companies enable each other and lose the permission when one leaves")
    void additionalLandPermissionsStackAndUpdateImmediately() {
        addCompany();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ThranduilsCompany());

        assertThat(gqs.getMaxLandsThisTurn(gd, player1.getId())).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.getMaxLandsThisTurn(gd, player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall can target the Company without another Elf")
    void landfallCanTargetItselfWithoutAnotherElf() {
        Permanent company = harness.addToBattlefieldAndReturn(player1, new ThranduilsCompany());
        harness.setHand(player1, java.util.List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, company.getId());
        harness.passBothPriorities();

        assertThat(company.getEffectivePower()).isEqualTo(5);
        assertThat(company.getEffectiveToughness()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, company, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent company = harness.addToBattlefieldAndReturn(player1, new ThranduilsCompany());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, java.util.List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(company.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, company, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Both permitted land plays trigger landfall independently")
    void bothLandPlaysTriggerLandfall() {
        Permanent company = harness.addToBattlefieldAndReturn(player1, new ThranduilsCompany());
        harness.addToBattlefield(player1, new ElvishMystic());
        harness.setHand(player1, java.util.List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, company.getId());
        harness.passBothPriorities();
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, company.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())
                .stream().filter(p -> p.getCard() instanceof Forest)).hasSize(2);
        assertThat(company.getEffectivePower()).isEqualTo(7);
        assertThat(company.getEffectiveToughness()).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, company, Keyword.VIGILANCE)).isTrue();
    }
}
