package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.GreatIntelligencesPlanVillainousChoiceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatIntelligencesPlan.class, Forest.class, GrizzlyBears.class})
class GreatIntelligencesPlanTest extends BaseCardTest {

    @Test
    void drawsThreeThenOpponentCanChooseToDiscardThree() {
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        List<Card> discarded = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, drawn);
        GrizzlyBears handSpell = new GrizzlyBears();
        harness.setHand(player2, discarded);
        cast(handSpell);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(
                GreatIntelligencesPlanVillainousChoiceEffect.DISCARD_OPTION,
                GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION);

        harness.handleListChoice(player2, GreatIntelligencesPlanVillainousChoiceEffect.DISCARD_OPTION);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handSpell,
                drawn.get(0), drawn.get(1), drawn.get(2));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(discarded);
    }

    @Test
    void letsControllerCastOneSpellFromHandForFree() {
        GrizzlyBears freeSpell = new GrizzlyBears();
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, drawn);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        cast(freeSpell);

        harness.handleListChoice(player2, GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
    }

    @Test
    void cannotTargetTheController() {
        harness.setHand(player1, List.of(new GreatIntelligencesPlan()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(GrizzlyBears... additionalHandCards) {
        List<Card> hand = new java.util.ArrayList<>();
        hand.add(new GreatIntelligencesPlan());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        addMana();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
