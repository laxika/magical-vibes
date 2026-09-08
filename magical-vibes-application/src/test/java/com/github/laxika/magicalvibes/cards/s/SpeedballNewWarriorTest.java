package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpeedballNewWarrior.class, Shock.class, Counterspell.class, GrizzlyBears.class})
class SpeedballNewWarriorTest extends BaseCardTest {

    @Test
    void opponentSpellTargetingSpeedballBoostsItAndCanBeRetargeted() {
        Permanent speedball = harness.addToBattlefieldAndReturn(player1, new SpeedballNewWarrior());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castShockAtSpeedball(speedball);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, speedball)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, speedball)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Speedball, New Warrior");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void decliningToRetargetKeepsTheOriginalTarget() {
        Permanent speedball = harness.addToBattlefieldAndReturn(player1, new SpeedballNewWarrior());
        castShockAtSpeedball(speedball);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, speedball)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, speedball)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Speedball, New Warrior");
    }

    @Test
    void boostStillResolvesIfTheTargetingSpellLeavesTheStack() {
        Permanent speedball = harness.addToBattlefieldAndReturn(player1, new SpeedballNewWarrior());
        castShockAtSpeedball(speedball);

        harness.passPriority(player2);
        harness.setHand(player1, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        UUID shockId = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Shock"))
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
        harness.castInstant(player1, 0, shockId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, speedball)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, speedball)).isEqualTo(4);
    }

    private void castShockAtSpeedball(Permanent speedball) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, speedball.getId());
    }
}
