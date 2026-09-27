package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZaxaraTheExemplary.class, Hurricane.class, GrizzlyBears.class})
class ZaxaraTheExemplaryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds two mana of one chosen color")
    void addsTwoManaOfChosenColor() {
        Permanent zaxara = harness.addToBattlefieldAndReturn(player1, new ZaxaraTheExemplary());
        zaxara.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(zaxara.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting an X spell creates a Hydra with X plus-one-plus-one counters")
    void xSpellCreatesHydraWithXCounters() {
        harness.addToBattlefield(player1, new ZaxaraTheExemplary());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hydra.getEffectivePower()).isEqualTo(3);
        assertThat(hydra.getEffectiveToughness()).isEqualTo(3);
        assertThat(hydra.getCard().getSubtypes()).contains(CardSubtype.HYDRA);
    }

    @Test
    @DisplayName("Casting a spell without X does not create a Hydra")
    void nonXSpellDoesNotCreateHydra() {
        harness.addToBattlefield(player1, new ZaxaraTheExemplary());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hydra")).isZero();
    }
}
