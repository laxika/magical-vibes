package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

        harness.castAndResolveInstant(player1, 0, List.of(harness.getPermanentId(player2, "Llanowar Elves"), bear.getId()));

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
        harness.castAndResolveInstant(player1, 0, List.of(victimId, firstBear.getId(), secondBear.getId()));

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

        harness.castAndResolveInstant(player1, 0, List.of(harness.getPermanentId(player2, "Llanowar Elves"), bear.getId()));

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

    @Test
    @DisplayName("The surviving source gets boosted and deals damage when the other source leaves")
    void resolvesWithOneSourceRemoved() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareSpell(4);

        harness.castInstant(player1, 0, List.of(victim.getId(), firstBear.getId(), secondBear.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(firstBear);
        harness.passBothPriorities();

        assertThat(secondBear.getPowerModifier()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The source still gets boosted when the opposing target leaves")
    void boostsSourceWithVictimRemoved() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareSpell(4);

        harness.castInstant(player1, 0, List.of(victim.getId(), bear.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Terrific Team-Up");
    }

    @Test
    @DisplayName("The victim is unaffected when all source targets leave")
    void resolvesWithoutSourceDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareSpell(4);

        harness.castInstant(player1, 0, List.of(victim.getId(), bear.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        harness.passBothPriorities();

        assertThat(victim.getPowerModifier()).isZero();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Terrific Team-Up");
    }

    @Test
    @DisplayName("An opponent's large permanent does not reduce the casting cost")
    void opponentPermanentDoesNotReduceCost() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareSpell(2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(victim.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }
    @Test
    @DisplayName("The power boost expires at end of turn")
    void boostExpiresAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        prepareSpell(4);

        harness.castAndResolveInstant(player1, 0, List.of(victim.getId(), bear.getId()));
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(bear.getPowerModifier()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
    private void prepareSpell(int greenMana) {
        harness.setHand(player1, List.of(new TerrificTeamUp()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
    }
}
