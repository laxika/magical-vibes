package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyreticRitualTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has spell effect that awards 3 red mana")
    void hasSpellEffect() {
        PyreticRitual card = new PyreticRitual();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(AwardManaEffect.class);
        AwardManaEffect effect = (AwardManaEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.color()).isEqualTo(ManaColor.RED);
        assertThat(effect.amount()).isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Pyretic Ritual puts it on the stack as an instant spell")
    void castingPutsOnStack() {
        castPyreticRitual();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pyretic Ritual");
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Resolving adds three red mana to controller's mana pool")
    void resolvingAddsThreeRedMana() {
        castPyreticRitual();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castPyreticRitual();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Pyretic Ritual"));
    }

    // ===== Helpers =====

    private void castPyreticRitual() {
        harness.setHand(player1, List.of(new PyreticRitual()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
    }
}
