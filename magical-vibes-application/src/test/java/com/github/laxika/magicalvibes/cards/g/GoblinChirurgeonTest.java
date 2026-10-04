package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Aeolipile;
import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.f.FarrelitePriest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinChirurgeon.class, FarrelitePriest.class, Aeolipile.class, BoggartShenanigans.class})
class GoblinChirurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin regenerates the target creature")
    void sacrificesGoblinAndRegeneratesTarget() {
        harness.addToBattlefield(player1, new GoblinChirurgeon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FarrelitePriest());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Goblin Chirurgeon");

        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Chirurgeon cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GoblinChirurgeon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Aeolipile());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Sacrifice cost only accepts Goblins")
    void sacrificeCostOnlyAcceptsGoblins() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent otherGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new FarrelitePriest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FarrelitePriest());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, otherGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(source, nonGoblin)
                .doesNotContain(otherGoblin);
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target the source while sacrificing another Goblin")
    void canTargetSourceWhileSacrificingAnotherGoblin() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent sacrificedGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handlePermanentChosen(player1, sacrificedGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(source)
                .doesNotContain(sacrificedGoblin);
        assertThat(source.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature Goblin permanent can pay the sacrifice cost")
    void canSacrificeKindredGoblinEnchantment() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent goblinEnchantment = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handlePermanentChosen(player1, goblinEnchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.assertOnBattlefield(player1, "Goblin Chirurgeon");
        assertThat(source.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration prevents lethal damage, taps the creature, and clears damage")
    void regenerationPreventsLethalDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent sacrificedGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        harness.addToBattlefield(player2, new Aeolipile());

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handlePermanentChosen(player1, sacrificedGoblin.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Chirurgeon");
        assertThat(source.isTapped()).isTrue();
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(source.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A regeneration shield cannot prevent sacrificing the protected creature")
    void regenerationDoesNotPreventSacrifice() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent sacrificedGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FarrelitePriest());

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handlePermanentChosen(player1, sacrificedGoblin.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Goblin Chirurgeon");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing the targeted source leaves no legal target on resolution")
    void canSacrificeSourceWhileTargetingItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.assertInGraveyard(player1, "Goblin Chirurgeon");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Chirurgeon");
        assertThat(gd.stack).isEmpty();
        assertThat(source.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A tapped source can activate repeatedly by sacrificing other Goblins")
    void tappedSourceCanGrantMultipleShields() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        source.tap();

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handlePermanentChosen(player1, firstGoblin.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.handlePermanentChosen(player1, secondGoblin.getId());
        harness.passBothPriorities();

        assertThat(source.getRegenerationShield()).isEqualTo(2);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's Goblin cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsGoblin() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinChirurgeon());

        harness.activateAbility(player1, 0, 0, null, source.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, ownGoblin.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Chirurgeon");
        harness.assertNotInGraveyard(player2, "Goblin Chirurgeon");
        assertThat(source.getRegenerationShield()).isEqualTo(1);
    }
}
