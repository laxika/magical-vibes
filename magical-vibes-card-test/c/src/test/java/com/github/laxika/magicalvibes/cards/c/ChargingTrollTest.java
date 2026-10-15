package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChargingTroll.class, RagingKavu.class})
class ChargingTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance lets Charging Troll attack without tapping")
    void attacksWithoutTapping() {
        Permanent troll = addChargingTrollReady(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(troll.isAttacking()).isTrue();
        assertThat(troll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration can be activated with summoning sickness")
    void regenerationWorksWithSummoningSickness() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new ChargingTroll());
        troll.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each resolved activation grants a separate regeneration shield")
    void multipleActivationsGrantMultipleShields() {
        Permanent troll = addChargingTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(2);
        assertThat(troll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated regeneration clears lethal damage and removes Charging Troll from combat")
    void activatedRegenerationSavesFromCombat() {
        Permanent troll = addChargingTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new RagingKavu());
        attacker.setAttacking(true);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Charging Troll");
        harness.assertNotInGraveyard(player1, "Charging Troll");
        harness.assertInGraveyard(player2, "Raging Kavu");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.isBlocking()).isFalse();
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Resolving regeneration grants Charging Troll a regeneration shield")
    void resolvingRegenerationGrantsShield() {
        addChargingTrollReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Charging Troll");
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration can be activated while Charging Troll is tapped")
    void regenerationDoesNotRequireTapping() {
        Permanent troll = addChargingTrollReady(player1);
        troll.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Charging Troll from lethal combat damage")
    void regenerationSavesFromLethalCombat() {
        Permanent troll = addChargingTrollReady(player1);
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Charging Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Charging Troll dies without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent troll = addChargingTrollReady(player1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Charging Troll");
        harness.assertInGraveyard(player1, "Charging Troll");
    }

    private Permanent addChargingTrollReady(Player player) {
        return addCreatureReady(player, new ChargingTroll());
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        RagingKavu card = new RagingKavu();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
