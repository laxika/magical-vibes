package com.github.laxika.magicalvibes.cards.s;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StagecoachSecurity.class, GrizzlyBears.class})
class StagecoachSecurityTest extends BaseCardTest {

    private void castStagecoach() {
        harness.setHand(player1, List.of(new StagecoachSecurity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering boosts and grants vigilance to creatures you control")
    void boostsAndGrantsVigilance() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castStagecoach();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPowerModifiers()).isEqualTo(1);
        assertThat(bears.getToughnessModifiers()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        Permanent stagecoach = findPermanent(player1, "Stagecoach Security");
        assertThat(stagecoach.getPowerModifiers()).isEqualTo(1);
        assertThat(stagecoach.getToughnessModifiers()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, stagecoach, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not affect opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castStagecoach();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getPowerModifiers()).isZero();
        assertThat(bears.getToughnessModifiers()).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Plot exiles the card and lets it be cast for free on a later turn")
    void plotsAndCastsLater() {
        StagecoachSecurity stagecoach = new StagecoachSecurity();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(stagecoach));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).contains(stagecoach.getId());
        assertThat(gd.plottedCardIds).contains(stagecoach.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, stagecoach.getId()))
                .hasMessageContaining("on the turn it became plotted");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, stagecoach.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent plottedStagecoach = findPermanent(player1, "Stagecoach Security");
        assertThat(plottedStagecoach.getPowerModifiers()).isEqualTo(1);
        assertThat(plottedStagecoach.getToughnessModifiers()).isEqualTo(1);
    }
}
