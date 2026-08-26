package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatColony.class, GrizzlyBears.class, Plains.class})
class BatColonyTest extends BaseCardTest {

    @Test
    void createsOneBatForEachCaveManaSpentToCastIt() {
        harness.addToBattlefield(player1, cavePlains());
        harness.addToBattlefield(player1, cavePlains());
        harness.tapPermanent(player1, 0);
        harness.tapPermanent(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new BatColony()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countBats()).isEqualTo(2);
    }

    @Test
    void createsNoBatsWhenNoCaveManaWasSpent() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new BatColony()));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countBats()).isZero();
    }

    @Test
    void putsACounterOnTargetCreatureWhenACaveEnters() {
        harness.addToBattlefield(player1, new BatColony());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(cavePlains()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForANonCaveLand() {
        harness.addToBattlefield(player1, new BatColony());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Plains()));

        harness.playLand(player1, 0);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Card cavePlains() {
        Card cave = new Plains().createRuntimeCopy();
        cave.setSubtypes(List.of(CardSubtype.CAVE));
        return cave;
    }

    private long countBats() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bat"))
                .count();
    }
}
