package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindgraceAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Windgrace Acolyte has ETB mill controller and gain life effects")
    void hasCorrectEffects() {
        WindgraceAcolyte card = new WindgraceAcolyte();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0)).isInstanceOf(MillControllerEffect.class);
        assertThat(((MillControllerEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0)).count())
                .isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1)).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1)).amount())
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Casting Windgrace Acolyte puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new WindgraceAcolyte()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Windgrace Acolyte");
    }

    @Test
    @DisplayName("Resolving Windgrace Acolyte puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new WindgraceAcolyte()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Windgrace Acolyte"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Windgrace Acolyte");
    }

    @Test
    @DisplayName("ETB mills controller's top 3 cards into graveyard")
    void etbMillsThreeCards() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();
        Forest f3 = new Forest();

        harness.setHand(player1, List.of(new WindgraceAcolyte()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(f1, f2, f3));

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell, ETB triggers
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(graveyardBefore + 3);
    }

    @Test
    @DisplayName("ETB gains controller 3 life")
    void etbGainsThreeLife() {
        harness.setHand(player1, List.of(new WindgraceAcolyte()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell, ETB triggers
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("ETB mills controller, not opponent")
    void etbMillsControllerNotOpponent() {
        harness.setHand(player1, List.of(new WindgraceAcolyte()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(opponentGraveyardBefore);
    }
}
