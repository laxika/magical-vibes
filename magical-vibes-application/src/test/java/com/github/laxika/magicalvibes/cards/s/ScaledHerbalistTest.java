package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScaledHerbalist.class, Forest.class, GrizzlyBears.class})
class ScaledHerbalistTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a chosen land from hand onto the battlefield")
    void putsLandFromHandOntoBattlefield() {
        Permanent herbalist = addCreatureReady(player1, new ScaledHerbalist());
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice = gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard() == forest)).isTrue();
        assertThat(herbalist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the land choice leaves the hand unchanged")
    void decliningLandChoiceDoesNothing() {
        Permanent herbalist = addCreatureReady(player1, new ScaledHerbalist());
        Card forest = new Forest();
        harness.setHand(player1, List.of(forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard() == forest)).isTrue();
        assertThat(herbalist.isTapped()).isTrue();
    }
}
