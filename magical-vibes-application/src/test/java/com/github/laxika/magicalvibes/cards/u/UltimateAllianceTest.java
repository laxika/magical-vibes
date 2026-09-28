package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UltimateAlliance.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class})
class UltimateAllianceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of creatures you control")
    void dealsDamageEqualToControlledCreatureCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new UltimateAlliance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, hillGiant.getId());
        harness.passBothPriorities();

        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts creatures when the spell resolves")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new UltimateAlliance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, hillGiant.getId());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(hillGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new UltimateAlliance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
