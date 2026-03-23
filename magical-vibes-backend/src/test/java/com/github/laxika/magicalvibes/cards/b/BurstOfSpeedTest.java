package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurstOfSpeedTest extends BaseCardTest {

    @Test
    @DisplayName("Burst of Speed has correct card properties")
    void hasCorrectProperties() {
        BurstOfSpeed card = new BurstOfSpeed();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(grant.keywords()).containsExactly(Keyword.HASTE);
        assertThat(grant.scope()).isEqualTo(GrantScope.OWN_CREATURES);
    }

    @Test
    @DisplayName("Resolving Burst of Speed gives own creatures haste")
    void resolvesAndGrantsHaste() {
        Permanent p1a = addReadyCreature(player1, new GrizzlyBears());
        Permanent p1b = addReadyCreature(player1, new GrizzlyBears());
        Permanent p2 = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(p1a.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(p1b.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(p2.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Burst of Speed haste wears off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Casting Burst of Speed puts it on stack as sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BurstOfSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Burst of Speed");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
