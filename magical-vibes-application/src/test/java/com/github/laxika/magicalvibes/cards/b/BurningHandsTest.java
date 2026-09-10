package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurningHands.class, GarrukWildspeaker.class, GrizzlyBears.class, HillGiant.class})
class BurningHandsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a non-green creature")
    void dealsTwoDamageToNonGreenCreature() {
        Permanent target = addToBattlefield(player2, new HillGiant());

        castOn(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 6 damage to a green creature")
    void dealsSixDamageToGreenCreature() {
        Permanent target = addToBattlefield(player2, new GrizzlyBears());

        castOn(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 6 damage to a green planeswalker")
    void dealsSixDamageToGreenPlaneswalker() {
        Permanent target = new Permanent(new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 8);
        gd.playerBattlefields.get(player2.getId()).add(target);

        castOn(target);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addToBattlefield(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castOn(Permanent target) {
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BurningHands()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
