package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChoiceOfFortunes.class, GrizzlyBears.class})
class ChoiceOfFortunesTest extends BaseCardTest {

    @Test
    void acceptsTheShuffleAndSeeksTwoMoreCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        castChoiceOfFortunes(first, second);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.floatingEffectsOnPlayer(player1.getId(), GrantNoMaximumHandSizeEffect.class))
                .extracting(FloatingContinuousEffect::duration).contains(Duration.PERMANENT);
    }

    @Test
    void decliningTheShuffleKeepsTheFirstTwoCardsAndStillGrantsNoMaximumHandSize() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        castChoiceOfFortunes(first, second);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.floatingEffectsOnPlayer(player1.getId(), GrantNoMaximumHandSizeEffect.class))
                .extracting(FloatingContinuousEffect::duration).contains(Duration.PERMANENT);
    }

    private void castChoiceOfFortunes(Card first, Card second) {
        harness.setHand(player1, List.of(new ChoiceOfFortunes()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
