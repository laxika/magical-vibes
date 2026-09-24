package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrowthCharm.class, Forest.class, GrizzlyBears.class})
class GrowthCharmTest extends BaseCardTest {

    @Test
    void rampantGrowthModeFindsBasicLandAndPutsItOntoBattlefieldTapped() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
        prepareSpell();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped());
    }

    @Test
    void giantGrowthModeBoostsTargetCreatureUntilEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell();

        harness.castModalInstant(player1, 0, 1, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void regrowthModeReturnsTargetCardFromGraveyardToHand() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        prepareSpell();

        harness.castModalInstant(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void regrowthModeDoesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Card target = new GrizzlyBears();
        Card otherCard = new Forest();
        harness.setGraveyard(player1, List.of(target, otherCard));
        prepareSpell();

        harness.castModalInstant(player1, 0, 2, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new GrowthCharm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
