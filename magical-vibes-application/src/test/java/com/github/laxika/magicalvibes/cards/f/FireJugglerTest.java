package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.cards.s.Stenchskipper;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireJuggler.class, IndomitableAncients.class, PricklyBoggart.class, Stenchskipper.class})
class FireJugglerTest extends BaseCardTest {

    @Test
    @DisplayName("Winning the clash deals 4 damage to each creature blocking Fire Juggler")
    void wonClashDamagesBlockers() {
        addAttackingJuggler(player1);
        Permanent blocker1 = addCreatureReady(player2, new IndomitableAncients());
        Permanent blocker2 = addCreatureReady(player2, new IndomitableAncients());
        Permanent nonBlocker = addCreatureReady(player2, new IndomitableAncients());
        // Higher mana value on top for player1 (Stenchskipper MV 4 > Prickly Boggart MV 1)
        // → player1 wins.
        forcePlayer1ClashWin();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveClashKeepingCardsOnTop();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(blocker1.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(blocker2.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(nonBlocker.getId()));
        assertThat(blocker1.getMarkedDamage()).isEqualTo(4);
        assertThat(blocker2.getMarkedDamage()).isEqualTo(4);
        assertThat(nonBlocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Losing the clash leaves the blockers unharmed")
    void lostClashSparesBlockers() {
        addAttackingJuggler(player1);
        Permanent blocker = addCreatureReady(player2, new IndomitableAncients());
        // Lower mana value on top for player1 (Prickly Boggart MV 1 < Stenchskipper MV 4)
        // → player1 loses.
        harness.setLibrary(player1, List.of(new PricklyBoggart()));
        harness.setLibrary(player2, List.of(new Stenchskipper()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveClashKeepingCardsOnTop();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(blocker.getId()));
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Tying the clash does not deal damage to the blockers")
    void tiedClashSparesBlockers() {
        addAttackingJuggler(player1);
        Permanent blocker = addCreatureReady(player2, new IndomitableAncients());
        // Equal mana values do not produce a win for either player.
        harness.setLibrary(player1, List.of(new PricklyBoggart()));
        harness.setLibrary(player2, List.of(new PricklyBoggart()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveClashKeepingCardsOnTop();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(blocker.getId()));
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Winning the clash kills a blocker with less than 4 toughness")
    void wonClashKillsBlocker() {
        addAttackingJuggler(player1);
        addCreatureReady(player2, new PricklyBoggart());
        forcePlayer1ClashWin();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveClashKeepingCardsOnTop();

        harness.assertInGraveyard(player2, "Prickly Boggart");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An unblocked Fire Juggler does not clash")
    void unblockedJugglerDoesNotClash() {
        addAttackingJuggler(player1);
        addCreatureReady(player2, new IndomitableAncients());
        forcePlayer1ClashWin();

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gameLogContains("clashes:")).isFalse();
    }

    @Test
    @DisplayName("Both clash cards move only after both placement choices are made")
    void clashPlacementMovesCardsSimultaneously() {
        addAttackingJuggler(player1);
        Permanent blocker = addCreatureReady(player2, new IndomitableAncients());
        Stenchskipper firstTop = new Stenchskipper();
        PricklyBoggart firstSecond = new PricklyBoggart();
        PricklyBoggart secondTop = new PricklyBoggart();
        Stenchskipper secondSecond = new Stenchskipper();
        harness.setLibrary(player1, List.of(firstTop, firstSecond));
        harness.setLibrary(player2, List.of(secondTop, secondSecond));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstTop, firstSecond);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstSecond, firstTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondSecond, secondTop);
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    private void resolveClashKeepingCardsOnTop() {
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.passBothPriorities();
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
            gs.handleInteractionAnswer(gd, player2,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        });
    }

    private void forcePlayer1ClashWin() {
        harness.setLibrary(player1, List.of(new Stenchskipper()));
        harness.setLibrary(player2, List.of(new PricklyBoggart()));
    }

    private Permanent addAttackingJuggler(Player player) {
        Permanent perm = addCreatureReady(player, new FireJuggler());
        perm.setAttacking(true);
        return perm;
    }
}
