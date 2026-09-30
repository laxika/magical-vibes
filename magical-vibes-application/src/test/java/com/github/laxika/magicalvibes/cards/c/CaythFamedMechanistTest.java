package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaythFamedMechanist.class, GrizzlyBears.class})
class CaythFamedMechanistTest extends BaseCardTest {

    @Test
    void fabricatesItselfWithACounter() {
        Permanent cayth = castCayth(0);

        assertThat(cayth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void givesOtherNontokenCreaturesFabricate() {
        harness.addToBattlefield(player1, new CaythFamedMechanist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityPopulates() {
        Permanent cayth = castCayth(1);
        cayth.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Populate");
        harness.passBothPriorities();

        assertThat(countPermanentsBySubtype(player1, CardSubtype.SERVO)).isEqualTo(2);
    }

    @Test
    void activatedAbilityProliferates() {
        Permanent cayth = castCayth(0);
        cayth.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Proliferate");
        harness.handleMultiplePermanentsChosen(player1, List.of(cayth.getId()));

        assertThat(cayth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent castCayth(int mode) {
        harness.setHand(player1, List.of(new CaythFamedMechanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Cayth, Famed Mechanist");
    }

    private long countPermanentsBySubtype(com.github.laxika.magicalvibes.model.Player player,
                                          CardSubtype subtype) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(subtype))
                .count();
    }
}
