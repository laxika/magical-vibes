package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraspingTentacles.class, Forest.class, Ornithopter.class})
class GraspingTentaclesTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent mills eight, then accepting may returns an artifact under your control")
    void millsThenMayReturnArtifact() {
        Ornithopter artifact = new Ornithopter();
        List<Card> milledCards = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, milledCards);
        harness.setGraveyard(player2, List.of(artifact));

        castGraspingTentacles();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(artifact)
                .containsAll(milledCards)
                .hasSize(9);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getId))
                .contains(artifact.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .doesNotContain(artifact);
    }

    @Test
    @DisplayName("Declining the may leaves the opponent's artifact in their graveyard")
    void decliningMayLeavesArtifact() {
        Ornithopter artifact = new Ornithopter();
        List<Card> milledCards = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, milledCards);
        harness.setGraveyard(player2, List.of(artifact));

        castGraspingTentacles();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getId))
                .doesNotContain(artifact.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(artifact)
                .hasSize(9);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new GraspingTentacles()));
        addManaForGraspingTentacles();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGraspingTentacles() {
        harness.setHand(player1, List.of(new GraspingTentacles()));
        addManaForGraspingTentacles();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void addManaForGraspingTentacles() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
