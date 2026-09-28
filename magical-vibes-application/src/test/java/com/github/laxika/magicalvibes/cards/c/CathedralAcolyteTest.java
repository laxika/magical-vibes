package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CathedralAcolyte.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class CathedralAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Gives ward {1} to your creatures with any counter")
    void givesWardToYourCounteredCreatures() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent protectedCreature = addCreatureReady(player1, new HillGiant());
        protectedCreature.setCounterCount(CounterType.CHARGE, 1);
        Permanent unprotectedCreature = addCreatureReady(player1, new HillGiant());

        castShockFromOpponent(protectedCreature);
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");

        castShockFromOpponent(unprotectedCreature);
        assertThat(unprotectedCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps to put a +1/+1 counter on a creature that entered this turn")
    void putsCounterOnCreatureEnteredThisTurn() {
        Permanent acolyte = addCreatureReady(player1, new CathedralAcolyte());
        Card creatureCard = new GrizzlyBears();
        harness.setHand(player1, List.of(creatureCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, creatureCard);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(acolyte.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that entered before this turn")
    void cannotTargetCreatureFromEarlierTurn() {
        addCreatureReady(player1, new CathedralAcolyte());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn");
    }

    private void castShockFromOpponent(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
