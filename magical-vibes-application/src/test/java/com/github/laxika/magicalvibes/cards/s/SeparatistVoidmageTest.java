package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeparatistVoidmage.class, TimberpackWolf.class, Forest.class, Disperse.class})
class SeparatistVoidmageTest extends BaseCardTest {

    private void castVoidmage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SeparatistVoidmage(), "{3}{U}");
    }

    @Test
    @DisplayName("Accepting the may bounces the targeted opponent creature to its owner's hand")
    void acceptingBouncesOpponentCreature() {
        UUID wolfId = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf()).getId();
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wolfId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Timberpack Wolf");
        harness.assertInHand(player2, "Timberpack Wolf");
        harness.assertOnBattlefield(player1, "Separatist Voidmage");
    }

    @Test
    @DisplayName("Declining the may leaves the creature on the battlefield")
    void decliningLeavesCreature() {
        UUID wolfId = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf()).getId();
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wolfId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Timberpack Wolf");
        harness.assertOnBattlefield(player1, "Separatist Voidmage");
    }

    @Test
    @DisplayName("Can bounce a creature you control")
    void canBounceOwnCreature() {
        UUID wolfId = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf()).getId();
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wolfId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Timberpack Wolf");
        harness.assertInHand(player1, "Timberpack Wolf");
    }

    @Test
    @DisplayName("Can bounce itself when it is the only creature")
    void canBounceItself() {
        castVoidmage();
        harness.passBothPriorities();
        UUID voidmageId = harness.getPermanentId(player1, "Separatist Voidmage");
        harness.handlePermanentChosen(player1, voidmageId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Separatist Voidmage");
        harness.assertInHand(player1, "Separatist Voidmage");
    }

    @Test
    @DisplayName("Non-creature permanents are not legal targets")
    void cannotTargetNonCreature() {
        UUID forestId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        castVoidmage();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forestId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The target prompt appears because the Voidmage itself is always a legal target")
    void promptAppearsWithNoOtherCreatures() {
        castVoidmage();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("The ability still resolves after the Voidmage leaves the battlefield")
    void abilityResolvesAfterSourceLeaves() {
        UUID wolfId = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf()).getId();
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wolfId);
        UUID voidmageId = harness.getPermanentId(player1, "Separatist Voidmage");
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, voidmageId);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Separatist Voidmage");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Timberpack Wolf");
        harness.assertInHand(player2, "Timberpack Wolf");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not resolve when its target leaves before resolution")
    void abilityDoesNotResolveAfterTargetLeaves() {
        UUID wolfId = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf()).getId();
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wolfId);
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, wolfId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player2, "Timberpack Wolf");
        harness.assertOnBattlefield(player1, "Separatist Voidmage");
    }

    @Test
    @DisplayName("A creature controlled by another player returns to its owner's hand")
    void returnsCreatureToOwnerRatherThanController() {
        TimberpackWolf wolf = new TimberpackWolf();
        UUID wolfId = harness.addToBattlefieldAndReturn(player2, wolf).getId();
        gd.stolenCreatures.put(wolfId, player1.getId());
        castVoidmage();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wolfId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Timberpack Wolf");
        harness.assertInHand(player1, "Timberpack Wolf");
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(wolf);
    }
}
