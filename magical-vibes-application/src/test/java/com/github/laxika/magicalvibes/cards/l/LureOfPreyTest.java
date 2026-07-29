package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LureOfPreyTest extends BaseCardTest {

    @Test
    @DisplayName("After an opponent casts a creature spell, puts a green creature from hand onto the battlefield")
    void putsGreenCreatureAfterOpponentCreatureSpell() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Not castable when no opponent cast a creature spell this turn")
    void notCastableWithoutOpponentCreatureSpell() {
        harness.setHand(player1, List.of(new LureOfPrey(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Your own creature spell does not enable the cast")
    void ownCreatureSpellDoesNotEnableCast() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new LureOfPrey(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Declining the may leaves the creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only green creature cards are eligible to be put onto the battlefield")
    void onlyGreenCreaturesAreEligible() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LureOfPrey(), new SuntailHawk()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Suntail Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
