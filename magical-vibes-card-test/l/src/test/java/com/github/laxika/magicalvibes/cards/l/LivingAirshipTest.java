package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PhyrexianGargantua;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingAirship.class, LlanowarDead.class, PhyrexianGargantua.class})
class LivingAirshipTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Living Airship")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent airship = addAirshipReady(player1);
        Permanent blocker = addCreatureReady(player2, new LlanowarDead());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(airship)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving the regeneration ability grants a regeneration shield")
    void resolvingRegenerationGrantsShield() {
        addAirshipReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent airship = findPermanent(player1, "Living Airship");
        assertThat(airship.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateRegenerationWithoutEnoughMana() {
        addAirshipReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot pay the green component of regeneration with only colorless mana")
    void cannotActivateRegenerationWithoutGreenMana() {
        addAirshipReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Living Airship from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent airship = addAirshipReady(player1);
        airship.setRegenerationShield(1);
        airship.setBlocking(true);
        airship.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new PhyrexianGargantua());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Living Airship");
        Permanent survivedAirship = findPermanent(player1, "Living Airship");
        assertThat(survivedAirship.isTapped()).isTrue();
        assertThat(survivedAirship.getRegenerationShield()).isZero();
    }

    private Permanent addAirshipReady(Player player) {
        return addCreatureReady(player, new LivingAirship());
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void regenerationCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent airship = harness.addToBattlefieldAndReturn(player1, new LivingAirship());
        airship.setSummoningSick(true);
        airship.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(airship.getRegenerationShield()).isEqualTo(1);
        assertThat(airship.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations protect against two destructions but not a third")
    void repeatedActivationsProtectAgainstTwoDestructions() {
        Permanent airship = addAirshipReady(player1);
        for (int i = 0; i < 2; i++) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(airship.isTapped()).isFalse();
        assertThat(airship.getRegenerationShield()).isEqualTo(2);

        for (int shieldsRemaining = 1; shieldsRemaining >= 0; shieldsRemaining--) {
            airship.setMarkedDamage(1);
            assertThat(harness.getPermanentRemovalService().tryDestroyPermanent(gd, airship)).isFalse();
            harness.assertOnBattlefield(player1, "Living Airship");
            assertThat(airship.isTapped()).isTrue();
            assertThat(airship.getMarkedDamage()).isZero();
            assertThat(airship.getRegenerationShield()).isEqualTo(shieldsRemaining);
        }

        assertThat(harness.getPermanentRemovalService().tryDestroyPermanent(gd, airship)).isTrue();
        harness.assertNotOnBattlefield(player1, "Living Airship");
        harness.assertInGraveyard(player1, "Living Airship");
    }
}
