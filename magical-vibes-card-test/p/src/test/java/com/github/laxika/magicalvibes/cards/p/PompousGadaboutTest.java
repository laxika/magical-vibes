package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UndercoverCrocodelf;
import com.github.laxika.magicalvibes.cards.u.UnstableShapeshifter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PompousGadabout.class, UndercoverCrocodelf.class, Shock.class, UnstableShapeshifter.class})
class PompousGadaboutTest extends BaseCardTest {

    @Test
    @DisplayName("Pompous Gadabout has hexproof only during its controller's turn")
    void hexproofOnlyDuringControllerTurn() {
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, gadabout, Keyword.HEXPROOF)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, gadabout, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Pompous Gadabout cannot be blocked by a face-down creature")
    void cannotBeBlockedByFaceDownCreature() {
        Permanent blocker = addCreatureReady(player2, new UndercoverCrocodelf());
        blocker.setFaceDownAsDisguised();
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());
        gadabout.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(gadabout);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pompous Gadabout can be blocked by a creature with a name")
    void canBeBlockedByNamedCreature() {
        Permanent blocker = addCreatureReady(player2, new UndercoverCrocodelf());
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());
        gadabout.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(gadabout);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void opponentCannotTargetDuringControllerTurn() {
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());
        harness.forceActivePlayer(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, gadabout.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void opponentCanTargetDuringOpponentTurn() {
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, gadabout.getId());

        harness.assertNotOnBattlefield(player1, "Pompous Gadabout");
        harness.assertInGraveyard(player1, "Pompous Gadabout");
    }

    @Test
    void controllerCanTargetDuringOwnTurn() {
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, gadabout.getId());

        harness.assertNotOnBattlefield(player1, "Pompous Gadabout");
        harness.assertInGraveyard(player1, "Pompous Gadabout");
    }

    @Test
    void cannotBeBlockedByFaceUpCopyOfNamelessCreature() {
        Permanent gadabout = addCreatureReady(player1, new PompousGadabout());
        Permanent blocker = addCreatureReady(player2, new UnstableShapeshifter());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new UndercoverCrocodelf()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blocker.isFaceDown()).isFalse();
        assertThat(blocker.getCard().getName()).isNullOrEmpty();
        gadabout.setAttacking(true);
        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(gadabout);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }
}
