package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirNomadLegacy.class, GrizzlyBears.class, SuntailHawk.class})
class AirNomadLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Clue token when it enters")
    void createsClueOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new AirNomadLegacy());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Boosts flying creatures you control")
    void boostsOwnFlyingCreatures() {
        harness.addToBattlefield(player1, new AirNomadLegacy());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingHawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingHawk)).isEqualTo(1);
    }
}
