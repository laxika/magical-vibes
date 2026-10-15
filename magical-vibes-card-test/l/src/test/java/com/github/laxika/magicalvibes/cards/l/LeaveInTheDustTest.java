package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.cards.i.ImplementOfImprovement;
import com.github.laxika.magicalvibes.cards.s.SubmergedBoneyard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeaveInTheDust.class, AegisAutomaton.class, ImplementOfImprovement.class, SubmergedBoneyard.class})
class LeaveInTheDustTest extends BaseCardTest {

    @Test
    @DisplayName("Bounces target nonland permanent and draws a card")
    void bouncesAndDraws() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton()).getId();
        harness.setHand(player1, List.of(new LeaveInTheDust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Aegis Automaton");
        harness.assertInHand(player2, "Aegis Automaton");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SubmergedBoneyard()).getId();
        harness.setHand(player1, List.of(new LeaveInTheDust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Can return your own noncreature artifact and draw exactly one card")
    void returnsOwnNoncreatureArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new ImplementOfImprovement()).getId();
        harness.setLibrary(player1, List.of(new SubmergedBoneyard(), new AegisAutomaton()));
        harness.setHand(player1, List.of(new LeaveInTheDust()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Implement of Improvement");
        harness.assertInHand(player1, "Implement of Improvement");
        harness.assertInHand(player1, "Submerged Boneyard");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner rather than its controller")
    void returnsStolenPermanentToOwner() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton()).getId();
        gd.stolenCreatures.put(targetId, player1.getId());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new LeaveInTheDust()));
        harness.setLibrary(player1, List.of(new SubmergedBoneyard(), new AegisAutomaton()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Aegis Automaton");
        harness.assertInHand(player1, "Aegis Automaton");
        harness.assertNotInHand(player2, "Aegis Automaton");
        harness.assertInHand(player1, "Submerged Boneyard");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not draw when the only target leaves before resolution")
    void doesNotDrawWhenTargetLeaves() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton()).getId();
        harness.setHand(player1, List.of(new LeaveInTheDust()));
        harness.setHand(player2, List.of(new LeaveInTheDust()));
        harness.setLibrary(player1, List.of(new SubmergedBoneyard(), new AegisAutomaton()));
        harness.setLibrary(player2, List.of(new SubmergedBoneyard(), new AegisAutomaton()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Aegis Automaton");
        harness.assertInHand(player2, "Submerged Boneyard");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Leave in the Dust");
        harness.assertInGraveyard(player2, "Leave in the Dust");
        assertThat(gd.stack).isEmpty();
    }
}
