package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KarplusanStrider;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Drelnoch.class, KarplusanStrider.class})
class DrelnochTest extends BaseCardTest {

    @Test
    @DisplayName("When Drelnoch becomes blocked, its controller may draw two cards")
    void drawsTwoCardsWhenAccepted() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KarplusanStrider(), new KarplusanStrider()));

        addCreatureReady(player1, new Drelnoch());
        addCreatureReady(player2, new KarplusanStrider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining Drelnoch's ability does not draw cards")
    void doesNotDrawWhenDeclined() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KarplusanStrider(), new KarplusanStrider()));

        addCreatureReady(player1, new Drelnoch());
        addCreatureReady(player2, new KarplusanStrider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Drelnoch's ability triggers once when multiple creatures block it")
    void triggersOnceForMultipleBlockers() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KarplusanStrider(), new KarplusanStrider()));

        addCreatureReady(player1, new Drelnoch());
        addCreatureReady(player2, new KarplusanStrider());
        addCreatureReady(player2, new KarplusanStrider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggers = gd.stack.stream()
                .filter(stackEntry -> stackEntry.getCard().getName().equals("Drelnoch"))
                .count();
        assertThat(triggers).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Drelnoch's ability does not trigger when it is unblocked")
    void doesNotTriggerWhenUnblocked() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KarplusanStrider(), new KarplusanStrider()));

        addCreatureReady(player1, new Drelnoch());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Drelnoch's controller draws when an opponent blocks it")
    void controllerDrawsWhenOpponentBlocksIt() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new KarplusanStrider(), new KarplusanStrider()));

        addCreatureReady(player2, new Drelnoch());
        addCreatureReady(player1, new KarplusanStrider());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Drelnoch does not trigger when it blocks another creature")
    void doesNotTriggerAsBlocker() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new KarplusanStrider(), new KarplusanStrider()));

        addCreatureReady(player1, new KarplusanStrider());
        addCreatureReady(player2, new Drelnoch());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Drelnoch's blocked trigger still draws after Drelnoch dies")
    void drawsAfterSourceDies() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KarplusanStrider(), new KarplusanStrider()));

        var drelnoch = addCreatureReady(player1, new Drelnoch());
        addCreatureReady(player2, new KarplusanStrider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        drelnoch.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Drelnoch");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
