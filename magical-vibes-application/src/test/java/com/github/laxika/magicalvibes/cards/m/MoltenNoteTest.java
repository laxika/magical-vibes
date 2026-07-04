package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToManaSpentToCastEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoltenNoteTest extends BaseCardTest {

    @Test
    @DisplayName("Has mana-spent damage and untap creatures effects")
    void hasCorrectStructure() {
        MoltenNote card = new MoltenNote();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(DealDamageToTargetCreatureEqualToManaSpentToCastEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(UntapAllControlledPermanentsEffect.class);
    }

    @Test
    @DisplayName("Deals damage equal to total mana spent and untaps your creatures")
    void dealsManaSpentDamageAndUntaps() {
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        ally.tap();

        harness.setHand(player1, List.of(new MoltenNote()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 3, enemy.getId());
        harness.passBothPriorities();

        assertThat(enemy.getMarkedDamage()).isEqualTo(5);
        assertThat(ally.isTapped()).isFalse();
    }
}
