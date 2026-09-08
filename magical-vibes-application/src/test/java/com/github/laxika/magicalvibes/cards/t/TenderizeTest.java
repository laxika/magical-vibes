package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tenderize.class, GrizzlyBears.class, HillGiant.class})
class TenderizeTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control deals its power as damage to an opponent's creature")
    void dealsPowerDamageToOpponentsCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Tenderize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The target survives when the source's power is less than its toughness")
    void dealsOnlySourcePowerDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Tenderize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Both targets must have the required controllers")
    void rejectsInvalidControllers() {
        Permanent ownSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Tenderize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownSource.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }
}
