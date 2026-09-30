package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RibaldShanty.class, GrizzlyBears.class, HillGiant.class, Plains.class})
class RibaldShantyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its starting intensity")
    void dealsDamageEqualToStartingIntensity() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        RibaldShanty shanty = new RibaldShanty();
        harness.setHand(player1, List.of(shanty));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getCardIntensity(shanty.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Intensifies all owned Chorus cards")
    void intensifiesOwnedChorusCards() {
        RibaldShanty shanty = new RibaldShanty();
        RibaldShanty otherShanty = new RibaldShanty();
        RibaldShanty opponentShanty = new RibaldShanty();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(shanty, otherShanty));
        harness.setLibrary(player2, List.of(opponentShanty));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(shanty.getId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(otherShanty.getId())).isEqualTo(1);
        assertThat(gd.getCardIntensity(opponentShanty.getId())).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new RibaldShanty()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
