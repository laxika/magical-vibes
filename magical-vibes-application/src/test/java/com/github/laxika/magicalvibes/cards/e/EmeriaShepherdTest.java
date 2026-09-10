package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmeriaShepherd.class, Forest.class, GrizzlyBears.class, Plains.class})
class EmeriaShepherdTest extends BaseCardTest {

    @Test
    void nonPlainsLandfallReturnsTargetToHand() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
    }

    @Test
    void PlainsLandfallMayReturnTargetToBattlefield() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(returned);
    }

    @Test
    void decliningPlainsLandfallReturnsTargetToHand() {
        harness.addToBattlefield(player1, new EmeriaShepherd());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);
        chooseLandfallTarget(returned);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(returned.getId()));
    }

    private void chooseLandfallTarget(Card returned) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
    }
}
