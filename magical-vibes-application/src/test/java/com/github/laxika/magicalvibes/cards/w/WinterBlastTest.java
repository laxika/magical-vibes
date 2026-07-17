package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WinterBlastTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps both target creatures but only the flier takes 2 damage")
    void tapsAllTargetsDamagesOnlyFliers() {
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent grounded = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2: {2}{G} = 3

        harness.castSorcery(player1, 0, 2, List.of(flier.getId(), grounded.getId()));
        harness.passBothPriorities();

        assertThat(flier.isTapped()).isTrue();
        assertThat(grounded.isTapped()).isTrue();
        assertThat(flier.getMarkedDamage()).isEqualTo(2);
        assertThat(grounded.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("2 damage kills a targeted 1/1 flier")
    void killsLowToughnessFlier() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 2); // X=1: {1}{G} = 2

        harness.castSorcery(player1, 0, 1, List.of(hawk.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WinterBlast()));
        harness.addMana(player1, ManaColor.GREEN, 2); // X=1

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
