package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SunCeYoungConquererTest extends BaseCardTest {

    private void castSunCe() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SunCeYoungConquerer()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("ETB triggers a may prompt when a creature is on the battlefield")
    void etbTriggersMayPrompt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSunCe();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting returns the target creature to its owner's hand")
    void acceptingBouncesTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castSunCe();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the may does not bounce anything")
    void decliningMayDoesNotBounce() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSunCe();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Sun Ce enters the battlefield after resolution")
    void sunCeEntersBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castSunCe();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack -> may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sun Ce, Young Conquerer"));
    }
}
