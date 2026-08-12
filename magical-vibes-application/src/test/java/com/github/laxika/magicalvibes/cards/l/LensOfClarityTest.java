package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LensOfClarityTest extends BaseCardTest {

    @Test
    @DisplayName("Only its controller can see the top card of their library")
    void onlyControllerSeesOwnLibraryTopCard() {
        harness.addToBattlefield(player1, new LensOfClarity());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can look at an opponent's face-down creature without paying mana")
    void looksAtOpponentsFaceDownCreature() {
        harness.addToBattlefield(player1, new LensOfClarity());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, faceDownCreature.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT"))
                .anyMatch(message -> message.contains("Master of Pearls"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
    }

    @Test
    @DisplayName("Cannot look at a face-down creature you control")
    void cannotLookAtOwnFaceDownCreature() {
        harness.addToBattlefield(player1, new LensOfClarity());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player1, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faceDownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
