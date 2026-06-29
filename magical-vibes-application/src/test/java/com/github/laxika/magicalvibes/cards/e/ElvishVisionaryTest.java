package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElvishVisionaryTest extends BaseCardTest {


    @Test
    @DisplayName("Elvish Visionary has ETB draw card effect")
    void hasEtbDrawEffect() {
        ElvishVisionary card = new ElvishVisionary();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Casting Elvish Visionary puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Elvish Visionary");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elvish Visionary"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Elvish Visionary");
    }

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }
}
