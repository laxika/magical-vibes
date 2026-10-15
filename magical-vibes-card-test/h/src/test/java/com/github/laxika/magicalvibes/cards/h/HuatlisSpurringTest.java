package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuatlisSpurring.class, BishopsSoldier.class, HuatliDinosaurKnight.class})
class HuatlisSpurringTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +2/+0 to target creature without a Huatli planeswalker")
    void givesPlus2Plus0WithoutHuatli() {
        harness.addToBattlefield(player1, new BishopsSoldier());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Bishop's Soldier");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent("Bishop's Soldier");
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gives +4/+0 to target creature when controller controls a Huatli planeswalker")
    void givesPlus4Plus0WithHuatli() {
        harness.addToBattlefield(player1, new BishopsSoldier());
        harness.addToBattlefield(player1, new HuatliDinosaurKnight());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Bishop's Soldier");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent("Bishop's Soldier");
        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gives only +2/+0 if Huatli leaves the battlefield before resolution")
    void givesPlus2Plus0IfHuatliLostBeforeResolution() {
        harness.addToBattlefield(player1, new BishopsSoldier());
        harness.addToBattlefield(player1, new HuatliDinosaurKnight());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Bishop's Soldier");
        harness.castInstant(player1, 0, bearId);

        // Remove Huatli before resolution
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard() instanceof HuatliDinosaurKnight);

        harness.passBothPriorities();

        Permanent bear = findPermanent("Bishop's Soldier");
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Huatli planeswalker does not grant the upgrade")
    void opponentHuatliDoesNotCount() {
        harness.addToBattlefield(player1, new BishopsSoldier());
        harness.addToBattlefield(player2, new HuatliDinosaurKnight());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Bishop's Soldier");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent("Bishop's Soldier");
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new BishopsSoldier());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Bishop's Soldier");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent("Bishop's Soldier");
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new BishopsSoldier());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Bishop's Soldier");
        harness.castInstant(player1, 0, bearId);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Huatli entering before resolution grants the upgraded boost")
    void huatliEnteringBeforeResolutionGrantsUpgrade() {
        harness.addToBattlefield(player1, new BishopsSoldier());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Bishop's Soldier");
        harness.castInstant(player1, 0, targetId);
        harness.addToBattlefield(player1, new HuatliDinosaurKnight());
        harness.passBothPriorities();

        Permanent target = findPermanent("Bishop's Soldier");
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature receives the upgrade based on the spell controller's Huatli")
    void canUpgradeOpponentsCreature() {
        harness.addToBattlefield(player2, new BishopsSoldier());
        harness.addToBattlefield(player1, new HuatliDinosaurKnight());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Bishop's Soldier");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = findPermanent(player2, "Bishop's Soldier");
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The upgraded boost persists after Huatli leaves and expires at cleanup")
    void upgradedBoostIsFixedAtResolutionAndExpiresAtCleanup() {
        harness.addToBattlefield(player1, new BishopsSoldier());
        harness.addToBattlefield(player1, new HuatliDinosaurKnight());
        harness.setHand(player1, List.of(new HuatlisSpurring()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Bishop's Soldier");
        harness.castAndResolveInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard() instanceof HuatliDinosaurKnight);

        Permanent target = findPermanent("Bishop's Soldier");
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent findPermanent(String cardName) {
        return findPermanent(player1, cardName);
    }
}
