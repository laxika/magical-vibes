package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InscriptionOfInsight.class, CliffhavenSellSword.class})
class InscriptionOfInsightTest extends BaseCardTest {

    @Test
    void returnsUpToTwoCreaturesWithoutKicker() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0},
                List.of(first.getId(), second.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Cliffhaven Sell-Sword");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void scriesThenDrawsTwoCards() {
        harness.setLibrary(player1, List.of(new CliffhavenSellSword(), new CliffhavenSellSword(), new CliffhavenSellSword()));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void targetPlayerCreatesIllusionEqualToHandSize() {
        harness.setHand(player2, List.of(
                new CliffhavenSellSword(), new CliffhavenSellSword(), new CliffhavenSellSword(), new CliffhavenSellSword()));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2},
                List.of(player2.getId()), List.of());
        harness.passBothPriorities();

        List<Permanent> illusions = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(illusions).singleElement().satisfies(illusion -> {
            assertThat(illusion.getCard().getName()).isEqualTo("Illusion");
            assertThat(illusion.getEffectivePower()).isEqualTo(4);
            assertThat(illusion.getEffectiveToughness()).isEqualTo(4);
        });
    }

    @Test
    void kickerAllowsChoosingAllThreeModes() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setHand(player2, List.of(
                new CliffhavenSellSword(), new CliffhavenSellSword(), new CliffhavenSellSword(), new CliffhavenSellSword()));
        harness.setLibrary(player1, List.of(new CliffhavenSellSword(), new CliffhavenSellSword(), new CliffhavenSellSword()));
        harness.setHand(player1, List.of(new InscriptionOfInsight()));
        addMana(3, 5);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 3, new int[]{0, 1, 2}),
                null, null, List.of(first.getId(), second.getId(), player2.getId()),
                List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(illusion -> {
                    assertThat(illusion.getCard().isToken()).isTrue();
                    assertThat(illusion.getEffectivePower()).isEqualTo(6);
                    assertThat(illusion.getEffectiveToughness()).isEqualTo(6);
                });
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotChooseMultipleModesWithoutKicker() {
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{1, 2}, List.of(player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedSpellCanChooseNoModes() {
        harness.setHand(player1, List.of(new InscriptionOfInsight()));
        addMana(3, 5);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(0, 3, new int[]{}),
                null, null, List.of(), List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Inscription of Insight");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bounceModeCanChooseNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Inscription of Insight");
    }

    @Test
    void bounceModeCanChooseOneTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0},
                List.of(first.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first.getCard());
    }

    @Test
    void kickedDrawModeIncreasesSelfTargetedTokenSizeBeforeCreation() {
        harness.setLibrary(player1, List.of(new CliffhavenSellSword(), new CliffhavenSellSword(),
                new CliffhavenSellSword()));
        harness.setHand(player1, List.of(new InscriptionOfInsight()));
        addMana(3, 5);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 3, new int[]{1, 2}),
                null, null, List.of(player1.getId()), List.of(), false, null, null, null, null, null, true);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(illusion -> {
            assertThat(illusion.getCard().isToken()).isTrue();
            assertThat(illusion.getEffectivePower()).isEqualTo(2);
            assertThat(illusion.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    void scryBottomsSelectedCardBeforeDrawing() {
        CliffhavenSellSword bottomed = new CliffhavenSellSword();
        InscriptionOfInsight kept = new InscriptionOfInsight();
        CliffhavenSellSword next = new CliffhavenSellSword();
        harness.setLibrary(player1, List.of(bottomed, kept, next));
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(), List.of());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, next);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomed);
    }

    @Test
    void emptyHandCreatesZeroToughnessTokenThatDies() {
        harness.setHand(player2, List.of());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2},
                List.of(player2.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Inscription of Insight");
    }

    @Test
    void losingAllTargetsPreventsUntargetedDrawModeFromResolving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CliffhavenSellSword());
        harness.setLibrary(player1, List.of(new CliffhavenSellSword(), new CliffhavenSellSword()));
        harness.setHand(player1, List.of(new InscriptionOfInsight()));
        addMana(3, 5);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 3, new int[]{0, 1}),
                null, null, List.of(creature.getId()), List.of(), false, null, null, null, null, null, true);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Inscription of Insight");
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new InscriptionOfInsight()));
        addMana(1, 3);
    }

    private void addMana(int blue, int colorless) {
        harness.addMana(player1, ManaColor.BLUE, blue);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
