package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarbreachWhale.class, GrizzlyBears.class})
class StarbreachWhaleTest extends BaseCardTest {

    @Test
    @DisplayName("When Starbreach Whale enters, its controller surveils 2")
    void entersWithSurveilTwo() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new StarbreachWhale()));
        addNormalMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Can be cast using its warp cost")
    void canBeCastForWarpCost() {
        harness.setHand(player1, List.of(new StarbreachWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Starbreach Whale");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
