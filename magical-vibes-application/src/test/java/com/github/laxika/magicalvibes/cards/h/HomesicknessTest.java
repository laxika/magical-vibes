package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

class HomesicknessTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct targeting: player target plus up to two creatures")
    void hasCorrectStructure() {
        Homesickness card = new Homesickness();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardForTargetPlayerEffect.class);
        assertThat(card.getMinTargets()).isEqualTo(1);
        assertThat(card.getMaxTargets()).isEqualTo(3);
    }

    @Test
    @DisplayName("Target player draws two; both creatures are tapped and stunned")
    void drawsAndStunsTwoCreatures() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castInstant(player1, 0, List.of(player2.getId(), bear.getId(), spider.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolves drawing cards even when no creatures are targeted")
    void resolvesWithNoCreatureTargets() {
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castInstant(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Homesickness");
    }

    @Test
    @DisplayName("Cannot target a creature as the player target")
    void cannotTargetCreatureAsPlayer() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Homesickness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID bearId = bear.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }
}
