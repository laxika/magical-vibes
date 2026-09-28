package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipOnTheRing.class, GrizzlyBears.class})
class SlipOnTheRingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and returns a creature you own under your control, then tempts you by the Ring")
    void flickersOwnedCreatureAndTemptsByTheRing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID originalId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, originalId);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(gd.ringStates.get(player1.getId()).level()).isEqualTo(1);
        assertThat(gd.ringStates.get(player1.getId()).bearerId()).isEqualTo(returned.getId());
        assertThat(gqs.hasEffectiveSupertype(gd, returned, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature you do not own")
    void cannotTargetUnownedCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlipOnTheRing()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you own");
    }
}
