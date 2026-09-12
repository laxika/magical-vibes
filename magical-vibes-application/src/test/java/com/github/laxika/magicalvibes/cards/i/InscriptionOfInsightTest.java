package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({InscriptionOfInsight.class, GrizzlyBears.class})
class InscriptionOfInsightTest extends BaseCardTest {

    @Test
    void returnsUpToTwoCreaturesWithoutKicker() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0},
                List.of(first.getId(), second.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void scriesThenDrawsTwoCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
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
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
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
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
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

    private void prepareCard() {
        harness.setHand(player1, List.of(new InscriptionOfInsight()));
        addMana(1, 3);
    }

    private void addMana(int blue, int colorless) {
        harness.addMana(player1, ManaColor.BLUE, blue);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
