package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AbbeyGargoyles;
import com.github.laxika.magicalvibes.cards.a.AysenAbbey;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Jinx.class, AysenAbbey.class, AbbeyGargoyles.class})
class JinxTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes the chosen basic land type until end of turn")
    void targetLandBecomesChosenType() {
        Permanent land = castJinxOnOpponentLand();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The overridden land taps for the new type's mana")
    void overriddenLandTapsForNewColor() {
        harness.setHand(player1, List.of(new Jinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AysenAbbey());
        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(land);
        gs.tapPermanent(gd, player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("The override wears off at end of turn")
    void overrideWearsOffAtEndOfTurn() {
        Permanent land = castJinxOnOpponentLand();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).isEmpty();
    }

    @Test
    @DisplayName("Resolving schedules a draw that happens at the next upkeep")
    void schedulesDrawAtNextUpkeep() {
        castJinxOnOpponentLand();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);

        int handAfterResolution = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterResolution + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        harness.setHand(player1, List.of(new Jinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AbbeyGargoyles());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    /** Casts Jinx on the opponent's land and chooses Island. */
    private Permanent castJinxOnOpponentLand() {
        harness.setHand(player1, List.of(new Jinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AysenAbbey());

        harness.castInstant(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        return land;
    }
}
