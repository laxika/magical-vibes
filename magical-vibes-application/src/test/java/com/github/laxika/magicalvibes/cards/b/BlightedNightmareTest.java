package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightedNightmare.class, GrizzlyBears.class, HillGiant.class})
class BlightedNightmareTest extends BaseCardTest {

    @Test
    void boostsGraveyardCreatureAndReturnsItWithBlightX() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new BlightedNightmare()));
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.perpetualPowerToughnessModifiers)
                .containsEntry(bears.getId(), new PerpetualPowerToughnessModifier(1, 1));

        Permanent nightmare = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Blighted Nightmare"))
                .findFirst()
                .orElseThrow();
        int nightmareIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nightmare);
        harness.activateAbility(player1, nightmareIndex, 0, 2, bears.getId(), Zone.GRAVEYARD);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Blighted Nightmare");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedBears)).isEqualTo(3);
    }

    @Test
    void cannotChooseXAboveGreatestControlledToughness() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefieldAndReturn(player1, new BlightedNightmare());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 4, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Blighted Nightmare");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }
}
