package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

@CardUsed({ClearShot.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class})
class ClearShotTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creature +1/+1 and it deals boosted power damage, killing the target")
    void boostAndBiteKillsSmallCreature() {
        // Grizzly Bears 2/2 → 3/3 after +1/+1, deals 3 to Llanowar Elves (1/1)
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = bear.getId();
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(bearId, elvesId));

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Bite damages but does not kill a tougher creature")
    void biteDamagesButDoesNotKill() {
        // Grizzly Bears 2/2 → 3/3, deals 3 to Air Elemental (4/4)
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = bear.getId();
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveInstant(player1, 0, List.of(bearId, elementalId));

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bear2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID id1 = bear1.getId();
        UUID id2 = bear2.getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you don't control");
    }

    @Test
    @DisplayName("Cannot target opponent creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Boost still applies when second target removed before resolution")
    void boostAppliesWhenSecondTargetRemoved() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = bear.getId();
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals exactly boosted power damage without receiving damage in return")
    void dealsBoostedPowerDamageOnlyOneWay() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId(), elemental.getId()));

        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Clear Shot");
    }

    @Test
    @DisplayName("No damage is dealt when the first target leaves before resolution")
    void noDamageWhenFirstTargetRemoved() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, List.of(bear.getId(), elemental.getId()));

        harness.getGameData().playerBattlefields.get(player1.getId()).remove(bear);
        harness.passBothPriorities();

        assertThat(elemental.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Clear Shot");
    }

    @Test
    @DisplayName("An illegal first target is neither boosted nor used to deal damage")
    void noBoostOrDamageWhenFirstTargetChangesController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, List.of(bear.getId(), elemental.getId()));

        harness.getGameData().playerBattlefields.get(player1.getId()).remove(bear);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bear);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(elemental.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Clear Shot");
    }

    @Test
    @DisplayName("Boost applies but no damage is dealt when the second target changes controller")
    void boostOnlyWhenSecondTargetChangesController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, List.of(bear.getId(), elemental.getId()));

        harness.getGameData().playerBattlefields.get(player2.getId()).remove(elemental);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(elemental);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);
        assertThat(elemental.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ClearShot()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, List.of(bear.getId(), elemental.getId()));
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(elemental.getMarkedDamage()).isZero();
    }
}
