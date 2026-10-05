package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NestingBot.class, Shock.class})
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

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

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
        harness.castAndResolveInstant(caster, 0, targetId);
    }

    @Test
    void startsSpeedWhenItEntersAndDoesNotResetExistingSpeed() {
        harness.castFromHand(player1, new NestingBot(), "{W}");
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);

        gd.playerSpeeds.put(player1.getId(), 3);
        harness.castFromHand(player1, new NestingBot(), "{W}");
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void maxSpeedBonusUsesOnlyItsControllersSpeed() {
        Permanent bot = addCreatureReady(player1, new NestingBot());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);
        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(1);

        gd.playerSpeeds.put(player1.getId(), 4);
        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(2);

        gd.playerSpeeds.put(player1.getId(), 3);
        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(1);
    }

    @Test
    void increasesSpeedOnlyOncePerTurn() {
        addCreatureReady(player1, new NestingBot());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void doesNotIncreaseSpeedDuringOpponentsTurn() {
        addCreatureReady(player1, new NestingBot());
        harness.runStateBasedActions();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void doesNotIncreaseSpeedWhenItsControllerLosesLife() {
        addCreatureReady(player1, new NestingBot());
        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void createsServoForOpposingControllerAndKeepsTheirSpeedAfterDeath() {
        harness.addToBattlefield(player2, new NestingBot());
        harness.runStateBasedActions();
        killWithShock(player1, player2, "Nesting Bot");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nesting Bot");
        assertThat(countPermanents(player2, "Servo")).isEqualTo(1);
        assertThat(countPermanents(player1, "Servo")).isZero();
        assertThat(gd.playerSpeeds.get(player2.getId())).isEqualTo(1);
    }
}
