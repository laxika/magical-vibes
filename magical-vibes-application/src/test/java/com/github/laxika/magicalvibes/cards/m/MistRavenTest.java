package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MistRavenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Mist Raven enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMistRaven(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mist Raven");
    }

    @Test
    @DisplayName("ETB resolves: target creature is returned to owner's hand")
    void etbBouncesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMistRaven(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Mist Raven enters the battlefield after resolution")
    void mistRavenEntersBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMistRaven(player2, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mist Raven"));
    }

    @Test
    @DisplayName("Can bounce own creature")
    void canBounceOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castMistRaven(player1, "Grizzly Bears");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private void castMistRaven(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new MistRaven()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }
}
