package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallOfTheTitans.class, GrizzlyBears.class, Shock.class})
class FallOfTheTitansTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to each of two targets")
    void dealsXDamageToEachTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstantForX(player1, 0, 3, List.of(creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstantForX(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Fall of the Titans");
    }

    @Test
    @DisplayName("Casts for its surge cost after another spell was cast")
    void castsForSurgeCost() {
        harness.setHand(player1, List.of(new Shock(), new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fall of the Titans");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast for its surge cost before another spell was cast")
    void surgeCostRequiresAnotherSpell() {
        harness.setHand(player1, List.of(new FallOfTheTitans()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
