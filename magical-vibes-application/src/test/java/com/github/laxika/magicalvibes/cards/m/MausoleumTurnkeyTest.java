package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MausoleumTurnkey.class, GrizzlyBears.class, HolyDay.class})
class MausoleumTurnkeyTest extends BaseCardTest {

    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MausoleumTurnkey()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The opponent chooses a creature card from the controller's graveyard")
    void opponentChoosesCreatureCard() {
        GrizzlyBears creature = new GrizzlyBears();
        HolyDay noncreature = new HolyDay();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        castAndResolveEtb();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .hasMessageContaining("Not your turn");

        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Day");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No creature card in the graveyard produces no choice")
    void noCreatureCardDoesNothing() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mausoleum Turnkey");
        harness.assertInGraveyard(player1, "Holy Day");
    }
}
