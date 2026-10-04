package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({GuardianOfPilgrims.class})
class GuardianOfPilgrimsTest extends BaseCardTest {

    @Test
    @DisplayName("Can choose itself after entering an empty battlefield")
    void canTargetItself() {
        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent guardian = findPermanent(player1, "Guardian of Pilgrims");
        harness.handlePermanentChosen(player1, guardian.getId());
        assertThat(guardian.getEffectivePower()).isEqualTo(2);
        assertThat(guardian.getEffectiveToughness()).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(guardian.getEffectivePower()).isEqualTo(3);
        assertThat(guardian.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Entering without being cast triggers and removing the source does not stop the boost")
    void triggerResolvesWithoutItsSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GuardianOfPilgrims());
        harness.enterBattlefieldAndReturn(player1, new GuardianOfPilgrims());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB gives target creature +1/+1 until end of turn")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new GuardianOfPilgrims());
        UUID targetId = harness.getPermanentId(player2, "Guardian of Pilgrims");

        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent target = findPermanent(player2, "Guardian of Pilgrims");
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new GuardianOfPilgrims());
        UUID targetId = harness.getPermanentId(player2, "Guardian of Pilgrims");

        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntil(TurnStep.CLEANUP);

        Permanent target = findPermanent(player2, "Guardian of Pilgrims");
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target its own controller's creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GuardianOfPilgrims());
        UUID targetId = harness.getPermanentId(player1, "Guardian of Pilgrims");

        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Guardian of Pilgrims");
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GuardianOfPilgrims());
        UUID targetId = harness.getPermanentId(player2, "Guardian of Pilgrims");

        harness.setHand(player1, List.of(new GuardianOfPilgrims()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB → fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
