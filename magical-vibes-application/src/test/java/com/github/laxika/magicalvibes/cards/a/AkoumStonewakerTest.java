package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkoumStonewaker.class, Forest.class})
@DisplayName("Akoum Stonewaker")
class AkoumStonewakerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers payment for a hasty trampling Elemental")
    void landfallOffersPaymentForElemental() {
        addStonewaker();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
        assertThat(elemental.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining landfall creates no Elemental")
    void decliningLandfallCreatesNoElemental() {
        addStonewaker();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("The Elemental is exiled at the beginning of the next end step")
    void elementalIsExiledAtNextEndStep() {
        addStonewaker();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("An opponent's landfall does not trigger Akoum Stonewaker")
    void opponentLandfallDoesNotTrigger() {
        addStonewaker();
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    private Permanent addStonewaker() {
        return harness.addToBattlefieldAndReturn(player1, new AkoumStonewaker());
    }
}
