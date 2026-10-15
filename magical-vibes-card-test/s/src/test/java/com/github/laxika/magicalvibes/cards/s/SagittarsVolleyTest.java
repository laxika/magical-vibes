package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.cards.u.UnbreakableFormation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SagittarsVolley.class, SerraAngel.class, WindDrake.class, GrizzlyBears.class, UnbreakableFormation.class})
class SagittarsVolleyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target flier and damages opposing fliers")
    void destroysTargetAndDamagesOpposingFliers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent otherOpponentFlier = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Permanent ownFlier = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        Permanent opponentGroundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SagittarsVolley()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(otherOpponentFlier.getMarkedDamage()).isEqualTo(1);
        assertThat(ownFlier.getMarkedDamage()).isZero();
        assertThat(opponentGroundCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SagittarsVolley()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, groundCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Can destroy your own flier while damaging only opposing fliers")
    void canTargetOwnFlier() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent ownFlier = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        Permanent opposingFlier = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        harness.setHand(player1, List.of(new SagittarsVolley()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
        assertThat(ownFlier.getMarkedDamage()).isZero();
        assertThat(opposingFlier.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals no damage when its sole target leaves before resolution")
    void doesNothingWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent opposingFlier = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        harness.setHand(player1, List.of(new SagittarsVolley()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Wind Drake");
        assertThat(opposingFlier.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Sagittars' Volley");
    }

    @Test
    @DisplayName("An indestructible target survives and takes damage along with other opposing fliers")
    void damagesIndestructibleTargetAndOtherOpposingFliers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent opposingFlier = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        harness.setHand(player2, List.of(new UnbreakableFormation()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0);

        harness.setHand(player1, List.of(new SagittarsVolley()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertNotInGraveyard(player2, "Serra Angel");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingFlier.getMarkedDamage()).isEqualTo(1);
    }
}
