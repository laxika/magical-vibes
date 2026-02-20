package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianRagerTest {

    private GameTestHarness harness;
    private Player player1;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Phyrexian Rager has correct card properties")
    void hasCorrectProperties() {
        PhyrexianRager card = new PhyrexianRager();

        assertThat(card.getName()).isEqualTo("Phyrexian Rager");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).contains(CardSubtype.HORROR);
    }

    @Test
    @DisplayName("Has ETB draw then lose life effects")
    void hasEtbEffects() {
        PhyrexianRager card = new PhyrexianRager();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1)).isInstanceOf(LoseLifeEffect.class);
        assertThat(((LoseLifeEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1)).amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Phyrexian Rager puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PhyrexianRager()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Phyrexian Rager");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new PhyrexianRager()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Rager"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Phyrexian Rager");
    }

    @Test
    @DisplayName("ETB draws a card and loses 1 life")
    void etbDrawsAndLosesLife() {
        harness.setHand(player1, List.of(new PhyrexianRager()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}
