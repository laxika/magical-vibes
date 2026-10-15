package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastWhisperer.class, GrizzlyBears.class, Shock.class})
class BeastWhispererTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell draws a card")
    void creatureSpellDrawsCard() {
        harness.addToBattlefield(player1, new BeastWhisperer());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not draw a card")
    void noncreatureSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new BeastWhisperer());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent casting a creature spell does not draw a card")
    void opponentCreatureSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new BeastWhisperer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The draw trigger resolves before the creature spell")
    void drawsBeforeCreatureResolves() {
        harness.addToBattlefield(player1, new BeastWhisperer());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting Beast Whisperer does not trigger its own ability")
    void doesNotTriggerForItsOwnCast() {
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new BeastWhisperer()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beast Whisperer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Beast Whisperer triggers separately for a creature spell")
    void multipleWhisperersEachDrawOneCard() {
        harness.addToBattlefield(player1, new BeastWhisperer());
        harness.addToBattlefield(player1, new BeastWhisperer());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
