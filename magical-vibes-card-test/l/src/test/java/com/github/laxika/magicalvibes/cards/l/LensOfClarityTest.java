package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LensOfClarity.class, MasterOfPearls.class})
class LensOfClarityTest extends BaseCardTest {

    @Test
    @DisplayName("Only its controller can see the top card of their library")
    void onlyControllerSeesOwnLibraryTopCard() {
        harness.addToBattlefield(player1, new LensOfClarity());
        harness.setLibrary(player1, List.of(new MasterOfPearls()));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Master of Pearls"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Master of Pearls"));
    }

    @Test
    @DisplayName("Can look at an opponent's face-down creature without activation or priority")
    void looksAtOpponentsFaceDownCreature() {
        harness.addToBattlefield(player1, new LensOfClarity());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isNotEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(faceDownCreature.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Does not grant the opponent permission to look at other players' face-down creatures")
    void permissionIsOnlyForController() {
        harness.addToBattlefield(player1, new LensOfClarity());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player1, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn2().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isEmpty();
        assertThat(gqs.mayLookAtOpposingFaceDownCreatures(gd, player2.getId())).isFalse();
    }

    @Test
    void permissionAppliesToCreaturesThatEnterLater() {
        harness.addToBattlefield(player1, new LensOfClarity());
        harness.publishState();
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isNotEmpty();
    }

    @Test
    void bothPermissionsEndWhenLensLeavesBattlefield() {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new LensOfClarity());
        harness.setLibrary(player1, List.of(new MasterOfPearls()));
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lens));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isEmpty();
    }

    @Test
    void emptyLibraryDoesNotPreventLookingAtFaceDownCreatures() {
        harness.addToBattlefield(player1, new LensOfClarity());
        harness.setLibrary(player1, List.of());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Master of Pearls\""))
                .isNotEmpty();
    }
}
