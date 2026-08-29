package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NestingBotTest extends BaseCardTest {

    @Test
    void getsPlusOnePowerAtMaxSpeed() {
        Permanent bot = addCreatureReady(player1, new NestingBot());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(1);

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bot)).isEqualTo(1);
    }

    @Test
    void increasesSpeedWhenOpponentLosesLifeDuringYourTurn() {
        addCreatureReady(player1, new NestingBot());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        gd.playerSpeeds.put(player1.getId(), 3);

        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkLifeLossTriggers(gd, player2.getId(), 1));

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void createsServoWhenItDies() {
        harness.addToBattlefield(player1, new NestingBot());
        killWithShock(player2, player1, "Nesting Bot");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Servo"))
                .toList();

        assertThat(servos).hasSize(1);
        Permanent servo = servos.getFirst();
        assertThat(servo.getCard().getPower()).isEqualTo(1);
        assertThat(servo.getCard().getToughness()).isEqualTo(1);
        assertThat(servo.getCard().getColor()).isNull();
        assertThat(servo.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(servo.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(servo.getCard().getSubtypes()).contains(CardSubtype.SERVO);
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
