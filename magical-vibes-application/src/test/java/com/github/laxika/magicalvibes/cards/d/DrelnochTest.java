package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrelnochTest extends BaseCardTest {

    @Test
    @DisplayName("When Drelnoch becomes blocked, its controller may draw two cards")
    void drawsTwoCardsWhenAccepted() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        Permanent drelnoch = addDrelnoch(player1);
        drelnoch.setAttacking(true);
        addReadyCreature(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining Drelnoch's ability does not draw cards")
    void doesNotDrawWhenDeclined() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        Permanent drelnoch = addDrelnoch(player1);
        drelnoch.setAttacking(true);
        addReadyCreature(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Drelnoch's ability triggers once when multiple creatures block it")
    void triggersOnceForMultipleBlockers() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        Permanent drelnoch = addDrelnoch(player1);
        drelnoch.setAttacking(true);
        addReadyCreature(player2);
        addReadyCreature(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggers = gd.stack.stream()
                .filter(stackEntry -> stackEntry.getCard().getName().equals("Drelnoch"))
                .count();
        assertThat(triggers).isEqualTo(1);
    }

    @Test
    @DisplayName("Drelnoch's ability does not trigger when it is unblocked")
    void doesNotTriggerWhenUnblocked() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        Permanent drelnoch = addDrelnoch(player1);
        drelnoch.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addDrelnoch(Player player) {
        Permanent permanent = new Permanent(new Drelnoch());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
