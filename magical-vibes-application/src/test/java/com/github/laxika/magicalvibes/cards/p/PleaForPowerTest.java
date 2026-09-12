package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PleaForPower.class, Forest.class})
class PleaForPowerTest extends BaseCardTest {

    @Test
    @DisplayName("A time majority grants the caster an extra turn")
    void timeMajorityGrantsExtraTurn() {
        cast();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.TIME);

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tied vote makes the caster draw three cards")
    void tiedVoteDrawsThreeCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        cast();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);

        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);
    }

    private void cast() {
        harness.setHand(player1, List.of(new PleaForPower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.PleaForPowerChoice.OPTIONS);
        return choice;
    }
}
