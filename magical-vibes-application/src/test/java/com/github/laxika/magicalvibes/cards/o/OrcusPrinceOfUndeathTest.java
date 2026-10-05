package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcusPrinceOfUndeath.class, GrizzlyBears.class, HillGiant.class})
class OrcusPrinceOfUndeathTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode shrinks other creatures and makes its controller lose X life")
    void shrinksOtherCreaturesAndLosesLife() {
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castOrcus(0, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent orcus = findPermanent(player1, "Orcus, Prince of Undeath");
        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hillGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, orcus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, orcus)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The second mode returns up to X creatures within the total mana-value cap with haste")
    void returnsCreaturesWithinManaValueCapWithHaste() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBear, secondBear));

        castOrcus(1, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCard().getId()).isEqualTo(firstBear.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(secondBear.getId());
    }


    @Test
    @DisplayName("Entering without being cast still offers a choice of triggered ability mode")
    void enteringWithoutBeingCastOffersModeChoice() {
        harness.enterBattlefieldAndReturn(player1, new OrcusPrinceOfUndeath());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("The first mode with zero X leaves creatures and life totals unchanged")
    void firstModeWithZeroXDoesNothing() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOrcus(0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The second mode with zero X cannot return creature cards")
    void secondModeWithZeroXReturnsNothing() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castOrcus(1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The second mode can return multiple creatures at the exact mana-value cap and haste expires")
    void returnsMultipleCreaturesAndHasteExpires() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBear, secondBear));

        castOrcus(1, 4);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        List<Permanent> returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> List.of(firstBear.getId(), secondBear.getId())
                        .contains(permanent.getCard().getId()))
                .toList();
        assertThat(returned).hasSize(2);
        assertThat(returned).allSatisfy(permanent -> {
            assertThat(permanent.isTapped()).isFalse();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(returned).allSatisfy(permanent ->
                assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isFalse());
    }

    @Test
    @DisplayName("The first mode expires at cleanup and does not shrink creatures entering later")
    void shrinkExpiresAndDoesNotAffectLaterCreatures() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castOrcus(0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent bear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
        harness.assertLife(player1, 19);
    }

    private void castOrcus(int mode, int xValue) {
        harness.setHand(player1, List.of(new OrcusPrinceOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 2);
        harness.castModalInstantForX(player1, 0, mode, xValue, null);
    }
}
