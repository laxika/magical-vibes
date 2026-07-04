package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachCreatureFirstTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PracticedOffenseTest extends BaseCardTest {

    @Test
    @DisplayName("Has player and creature target effects with keyword choice")
    void hasCorrectStructure() {
        PracticedOffense card = new PracticedOffense();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(PutPlusOnePlusOneCounterOnEachCreatureFirstTargetPlayerControlsEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(GrantChosenKeywordToSecondTargetEffect.class);
        GrantChosenKeywordToSecondTargetEffect keywordEffect =
                (GrantChosenKeywordToSecondTargetEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(keywordEffect.options()).containsExactly(Keyword.DOUBLE_STRIKE, Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Puts +1/+1 on each creature target player controls and grants chosen keyword")
    void buffsPlayerCreaturesAndGrantsKeyword() {
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PracticedOffense()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(player1.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DOUBLE_STRIKE");

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
