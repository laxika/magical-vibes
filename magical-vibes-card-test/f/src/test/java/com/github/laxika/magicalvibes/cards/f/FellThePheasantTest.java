package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellThePheasant.class, GrizzlyBears.class, AirElemental.class, Unsummon.class})
class FellThePheasantTest extends BaseCardTest {

    @Test
    void dealsFiveDamageToTargetCreatureWithFlyingAndCreatesFood() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new FellThePheasant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Food");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void cannotTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FellThePheasant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    void createsFoodWhenTargetingOwnFlyingCreatureAndFoodCanBeSacrificedForLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FellThePheasant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Air Elemental");
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    void doesNotCreateFoodWhenTargetLeavesBattlefieldBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new FellThePheasant(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInHand(player2, "Air Elemental");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertInGraveyard(player1, "Fell the Pheasant");
        assertThat(target.getMarkedDamage()).isZero();
    }
}
