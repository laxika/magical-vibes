package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PainfulLesson.class, DuneBeetle.class})
class PainfulLessonTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two cards and loses 2 life")
    void resolvesAllEffectsOnOpponent() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castPainfulLessonTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castPainfulLessonTargeting(player1.getId());
        harness.passBothPriorities();

        // setHand sets hand to [PainfulLesson], casting removes it (0), then draws 2
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castPainfulLessonTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());

        harness.setHand(player1, List.of(new PainfulLesson()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, beetle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draws both cards before lethal life loss")
    void drawsBeforeLethalLifeLoss() {
        DuneBeetle firstCard = new DuneBeetle();
        DuneBeetle secondCard = new DuneBeetle();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(firstCard, secondCard));
        harness.setLife(player2, 2);

        castPainfulLessonTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard, secondCard);
        harness.assertLife(player2, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void castPainfulLessonTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new PainfulLesson()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
