package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.K9MarkI;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({GreatIntelligencesPlan.class, Forest.class, K9MarkI.class})
class GreatIntelligencesPlanTest extends BaseCardTest {

    @Test
    void drawsThreeThenOpponentCanChooseToDiscardThree() {
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        List<Card> discarded = List.of(new K9MarkI(), new K9MarkI(), new K9MarkI());
        harness.setLibrary(player1, drawn);
        K9MarkI handSpell = new K9MarkI();
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
        K9MarkI freeSpell = new K9MarkI();
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, drawn);
        harness.setHand(player2, List.of(new K9MarkI(), new K9MarkI(), new K9MarkI()));
        cast(freeSpell);

        harness.handleListChoice(player2, GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "K-9, Mark I");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
    }

    @Test
    void cannotTargetTheController() {
        harness.setHand(player1, List.of(new GreatIntelligencesPlan()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentWithEmptyHandCanChooseToDiscardNothing() {
        K9MarkI freeSpell = new K9MarkI();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        cast(freeSpell);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly(
                GreatIntelligencesPlanVillainousChoiceEffect.DISCARD_OPTION,
                GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION);
        harness.handleListChoice(player2, GreatIntelligencesPlanVillainousChoiceEffect.DISCARD_OPTION);

        assertThat(gd.playerHands.get(player1.getId())).contains(freeSpell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentCanAllowFreeCastingWhenControllerHasOnlyLands() {
        List<Card> opponentHand = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player2, opponentHand);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        cast();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player2, GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION);

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardChoiceDiscardsAsManyAsPossibleWithFewerThanThreeCards() {
        Forest discarded = new Forest();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        cast(new K9MarkI());

        harness.handleListChoice(player2, GreatIntelligencesPlanVillainousChoiceEffect.DISCARD_OPTION);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningFreeCastKeepsHandCardPrivate() {
        K9MarkI freeSpell = new K9MarkI();
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        cast(freeSpell);
        harness.handleListChoice(player2, GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION);
        int logSize = gd.gameLog.size();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(freeSpell);
        harness.assertNotOnBattlefield(player1, "K-9, Mark I");
        assertThat(gd.gameLog.subList(logSize, gd.gameLog.size()).stream()
                .map(GameLogEntry::plainText)).noneMatch(log -> log.contains("K-9, Mark I"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canCastNewlyDrawnSpellButOnlyOneSpell() {
        K9MarkI firstSpell = new K9MarkI();
        K9MarkI secondSpell = new K9MarkI();
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(firstSpell, secondSpell, new Forest()));
        cast();

        harness.handleListChoice(player2, GreatIntelligencesPlanVillainousChoiceEffect.CAST_OPTION);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(secondSpell).doesNotContain(firstSpell);
        harness.assertOnBattlefield(player1, "K-9, Mark I");
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast(K9MarkI... additionalHandCards) {
        List<Card> hand = new java.util.ArrayList<>();
        hand.add(new GreatIntelligencesPlan());
        hand.addAll(List.of(additionalHandCards));
        harness.setHand(player1, hand);
        addMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
