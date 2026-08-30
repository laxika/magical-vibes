package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({TerrificTeamUp.class, AirElemental.class, ColossalDreadmaw.class, GrizzlyBears.class, LlanowarElves.class})
class TerrificTeamUpTest extends BaseCardTest {

    @Test
    @DisplayName("One target creature gets +1/+0 and deals its boosted power to an opponent creature")
    void oneCreatureGetsBoostedAndDealsPowerDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        prepareSpell(4);

        harness.castInstant(player1, 0, List.of(harness.getPermanentId(player2, "Llanowar Elves"), bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Two target creatures each deal their boosted power to the same opponent creature")
    void twoCreaturesEachDealPowerDamage() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        prepareSpell(4);

        UUID victimId = harness.getPermanentId(player2, "Colossal Dreadmaw");
        harness.castInstant(player1, 0, List.of(victimId, firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(firstBear.getPowerModifier()).isEqualTo(1);
        assertThat(secondBear.getPowerModifier()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("The spell costs two less with a permanent of mana value four or greater")
    void costReductionAppliesWithLargePermanent() {
        harness.addToBattlefield(player1, new AirElemental());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        prepareSpell(2);

        harness.castInstant(player1, 0, List.of(harness.getPermanentId(player2, "Llanowar Elves"), bear.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The spell cannot be cast for two mana without the cost reduction")
    void costReductionDoesNotApplyWithoutLargePermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        prepareSpell(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(harness.getPermanentId(player2, "Llanowar Elves"), bear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    private void prepareSpell(int greenMana) {
        harness.setHand(player1, List.of(new TerrificTeamUp()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
    }
}
