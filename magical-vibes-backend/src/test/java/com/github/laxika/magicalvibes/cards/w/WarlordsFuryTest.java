package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarlordsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Warlord's Fury has correct effects configured")
    void hasCorrectEffects() {
        WarlordsFury card = new WarlordsFury();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GrantKeywordEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Warlord's Fury grants first strike to all controlled creatures and draws a card")
    void grantsFirstStrikeAndDrawsCard() {
        Permanent bear1 = addReadyCreature(player1, new GrizzlyBears());
        Permanent bear2 = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarlordsFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(bear1.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bear2.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("Warlord's Fury does not grant first strike to opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        Permanent ownBear = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentBear = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarlordsFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ownBear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(opponentBear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Warlord's Fury first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarlordsFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Warlord's Fury draws a card even with no creatures on the battlefield")
    void drawsCardWithNoCreatures() {
        harness.setHand(player1, List.of(new WarlordsFury()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        int handSizeAfterCast = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
