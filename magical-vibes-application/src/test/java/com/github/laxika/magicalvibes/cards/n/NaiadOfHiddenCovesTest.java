package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.t.ThrenodySinger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaiadOfHiddenCoves.class, Murder.class, GrizzlyBears.class, ThrenodySinger.class})
class NaiadOfHiddenCovesTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the cost of a flash creature during an opponent's turn")
    void reducesFlashCreatureCost() {
        harness.addToBattlefield(player1, new NaiadOfHiddenCoves());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ThrenodySinger()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Naiads cannot reduce colored mana requirements")
    void multipleNaiadsDoNotReduceColoredMana() {
        harness.addToBattlefield(player1, new NaiadOfHiddenCoves());
        harness.addToBattlefield(player1, new NaiadOfHiddenCoves());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ThrenodySinger()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The reduction applies to every spell, not just the first")
    void reducesSuccessiveSpells() {
        harness.addToBattlefield(player1, new NaiadOfHiddenCoves());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ThrenodySinger(), new ThrenodySinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Reduces spells you cast during an opponent's turn")
    void reducesYourSpellsDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new NaiadOfHiddenCoves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce your spells during your own turn")
    void doesNotReduceYourSpellsDuringYourTurn() {
        harness.addToBattlefield(player1, new NaiadOfHiddenCoves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce spells cast by an opponent")
    void doesNotReduceOpponentsSpells() {
        harness.addToBattlefield(player1, new NaiadOfHiddenCoves());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
