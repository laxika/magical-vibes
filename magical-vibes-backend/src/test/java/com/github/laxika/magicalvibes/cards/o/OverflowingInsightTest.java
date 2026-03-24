package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OverflowingInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct effects and targeting")
    void hasCorrectProperties() {
        OverflowingInsight card = new OverflowingInsight();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardForTargetPlayerEffect.class);
    }

    @Test
    @DisplayName("Target player draws seven cards")
    void targetPlayerDrawsSevenCards() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 7);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castOverflowingInsightTargeting(player1.getId());
        harness.passBothPriorities();

        // setHand sets hand to [OverflowingInsight], casting removes it (0), then draws 7
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        int player1HandBefore = gd.playerHands.get(player1.getId()).size();

        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        // Player 1's hand should only lose the card that was cast (setHand sets 1 card, cast removes it)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Overflowing Insight");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new OverflowingInsight()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOverflowingInsightTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new OverflowingInsight()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
