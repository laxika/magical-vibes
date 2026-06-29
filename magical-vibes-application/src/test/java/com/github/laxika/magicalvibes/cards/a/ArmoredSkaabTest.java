package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArmoredSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Armored Skaab has ETB mill controller effect for 4 cards")
    void hasEtbMillControllerEffect() {
        ArmoredSkaab card = new ArmoredSkaab();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(MillControllerEffect.class);
        assertThat(((MillControllerEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).count())
                .isEqualTo(4);
    }

    @Test
    @DisplayName("Casting Armored Skaab puts it on stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ArmoredSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Armored Skaab");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingCreaturePutsEtbOnStack() {
        harness.setHand(player1, List.of(new ArmoredSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Armored Skaab"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Armored Skaab");
    }

    @Test
    @DisplayName("ETB ability mills controller's top 4 cards into graveyard")
    void etbMillsFourCards() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();
        Forest f3 = new Forest();
        Forest f4 = new Forest();

        harness.setHand(player1, List.of(new ArmoredSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(f1, f2, f3, f4));

        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell, ETB triggers
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(graveyardBefore + 4);
    }

    @Test
    @DisplayName("ETB mills controller, not opponent")
    void etbMillsControllerNotOpponent() {
        harness.setHand(player1, List.of(new ArmoredSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(opponentGraveyardBefore);
    }

    @Test
    @DisplayName("ETB mills fewer cards if library has less than 4")
    void etbMillsFewerIfLibrarySmall() {
        Forest f1 = new Forest();
        Forest f2 = new Forest();

        harness.setHand(player1, List.of(new ArmoredSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(f1, f2));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }
}
