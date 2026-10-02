package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherAdept.class, RuneclawBear.class, Unsummon.class})
class AetherAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Aether Adept enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new RuneclawBear());
        castAetherAdept(player2, "Runeclaw Bear");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Aether Adept");
    }

    @Test
    @DisplayName("ETB resolves: target creature is returned to owner's hand")
    void etbBouncesTargetCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        castAetherAdept(player2, "Runeclaw Bear");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInHand(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Aether Adept enters the battlefield after resolution")
    void adeptEntersBattlefield() {
        harness.addToBattlefield(player2, new RuneclawBear());
        castAetherAdept(player2, "Runeclaw Bear");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Aether Adept");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackEmptyAfterResolution() {
        harness.addToBattlefield(player2, new RuneclawBear());
        castAetherAdept(player2, "Runeclaw Bear");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can bounce own creature")
    void canBounceOwnCreature() {
        harness.addToBattlefield(player1, new RuneclawBear());
        castAetherAdept(player1, "Runeclaw Bear");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInHand(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Must return itself when it is the only creature")
    void returnsItselfWhenOnlyCreature() {
        harness.setHand(player1, List.of(new AetherAdept()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Aether Adept"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aether Adept");
        harness.assertInHand(player1, "Aether Adept");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can choose itself even when another creature is present")
    void canChooseItselfWithOtherCreaturePresent() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new AetherAdept()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Aether Adept"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aether Adept");
        harness.assertNotOnBattlefield(player1, "Aether Adept");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Returns a creature to its owner rather than its controller")
    void returnsCreatureToOwner() {
        RuneclawBear bear = new RuneclawBear();
        bear.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, bear);
        castAetherAdept(player2, "Runeclaw Bear");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInHand(player1, "Runeclaw Bear");
        harness.assertNotInHand(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Bounce still resolves after Aether Adept leaves the battlefield")
    void bounceResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player2, new RuneclawBear());
        castAetherAdept(player2, "Runeclaw Bear");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Aether Adept"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aether Adept");
        harness.assertNotOnBattlefield(player1, "Aether Adept");
        harness.assertInHand(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bounce does nothing when its target has already left")
    void bounceDoesNothingAfterTargetLeaves() {
        harness.addToBattlefield(player2, new RuneclawBear());
        castAetherAdept(player2, "Runeclaw Bear");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Runeclaw Bear"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aether Adept");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Runeclaw Bear")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castAetherAdept(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new AetherAdept()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetId);
    }
}
