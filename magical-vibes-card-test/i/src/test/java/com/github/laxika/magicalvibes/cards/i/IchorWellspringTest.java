package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IchorWellspring.class, Forest.class, Shatter.class, SteelSabotage.class})
class IchorWellspringTest extends BaseCardTest {


    @Test
    @DisplayName("Casting Ichor Wellspring puts it on stack as artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ichor Wellspring");
    }

    @Test
    @DisplayName("Resolving artifact spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell

        harness.assertOnBattlefield(player1, "Ichor Wellspring");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ichor Wellspring");
    }

    @Test
    @DisplayName("ETB trigger draws a card")
    void etbDrawsCard() {
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertInHand(player1, "Forest");
    }


    @Test
    @DisplayName("Destroying Ichor Wellspring puts death trigger on stack and draws a card")
    void deathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new IchorWellspring());
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Use Shatter to destroy the Wellspring
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var targetId = harness.getPermanentId(player1, "Ichor Wellspring");
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities(); // resolve Shatter — destroys Wellspring

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve death trigger

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ichor Wellspring");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Returning Wellspring to hand does not trigger a draw, but its pending entry trigger still resolves")
    void bounceWhileEntryTriggerPending() {
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new SteelSabotage()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, harness.getPermanentId(player1, "Ichor Wellspring"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ichor Wellspring");
        harness.assertInHand(player1, "Ichor Wellspring");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Countering Wellspring does not trigger either draw ability")
    void counteredSpellDoesNotDraw() {
        IchorWellspring wellspring = new IchorWellspring();
        harness.setHand(player1, List.of(wellspring));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new SteelSabotage()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, wellspring.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Ichor Wellspring");
        harness.assertInGraveyard(player1, "Ichor Wellspring");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Destroying Wellspring before its entry trigger resolves draws once for each event")
    void destroyedWhileEntryTriggerPendingDrawsTwice() {
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Ichor Wellspring"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ichor Wellspring");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
