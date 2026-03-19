package com.github.laxika.magicalvibes.cards.v;

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
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VampiricFuryTest extends BaseCardTest {

    @Test
    @DisplayName("VampiricFury has correct effects configured")
    void hasCorrectEffects() {
        VampiricFury card = new VampiricFury();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(BoostAllOwnCreaturesEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GrantKeywordEffect.class);
    }

    @Test
    @DisplayName("Vampiric Fury gives Vampire creatures +2/+0 and first strike")
    void buffsVampireCreatures() {
        Permanent vampire = addReadyCreature(player1, new VampireAristocrat()); // 2/2 Vampire
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(vampire.getEffectivePower()).isEqualTo(4);  // 2 + 2
        assertThat(vampire.getEffectiveToughness()).isEqualTo(2);  // unchanged
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Vampiric Fury does not affect non-Vampire creatures")
    void doesNotBuffNonVampires() {
        Permanent bear = addReadyCreature(player1, new GrizzlyBears()); // 2/2 Bear
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Vampiric Fury does not affect opponent's Vampires")
    void doesNotBuffOpponentVampires() {
        Permanent ownVampire = addReadyCreature(player1, new VampireAristocrat());
        Permanent opponentVampire = addReadyCreature(player2, new VampireAristocrat());
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownVampire.getEffectivePower()).isEqualTo(4);
        assertThat(ownVampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        assertThat(opponentVampire.getEffectivePower()).isEqualTo(2);
        assertThat(opponentVampire.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Vampiric Fury effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent vampire = addReadyCreature(player1, new VampireAristocrat());
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(vampire.getEffectivePower()).isEqualTo(4);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getEffectivePower()).isEqualTo(2);
        assertThat(vampire.getEffectiveToughness()).isEqualTo(2);
        assertThat(vampire.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Casting Vampiric Fury puts it on the stack as an instant spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new VampiricFury()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Vampiric Fury");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
