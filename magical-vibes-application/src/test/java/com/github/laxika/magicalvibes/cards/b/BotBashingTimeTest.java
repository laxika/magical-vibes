package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BotBashingTime.class, AvatarOfMight.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class BotBashingTimeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage and exiles a creature that dies from it")
    void killsAndExilesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BotBashingTime()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles the target if it dies later in the same turn")
    void exilesTargetThatDiesLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new BotBashingTime(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Avatar of Might"));
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new BotBashingTime()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
