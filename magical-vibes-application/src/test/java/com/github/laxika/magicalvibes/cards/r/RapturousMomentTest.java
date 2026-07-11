package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RapturousMomentTest extends BaseCardTest {

    @Test
    @DisplayName("Rapturous Moment has correct effect structure")
    void hasCorrectStructure() {
        RapturousMoment card = new RapturousMoment();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(4);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) card.getEffects(EffectSlot.SPELL).get(0)).amount()).isEqualTo(new Fixed(3));
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DiscardEffect.class);
        assertThat(((DiscardEffect) card.getEffects(EffectSlot.SPELL).get(1)).amount()).isEqualTo(new Fixed(2));
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(AwardManaEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(3)).isInstanceOf(AwardManaEffect.class);
    }

    @Test
    @DisplayName("Casting Rapturous Moment puts it on the stack as a sorcery")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new RapturousMoment()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Resolving draws three, discards two, and adds UURRR")
    void resolvesDrawDiscardMana() {
        harness.setHand(player1, List.of(new RapturousMoment()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Drew three cards (spell left hand), now choose two to discard.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }
}
