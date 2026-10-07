package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtweftGambit.class, SafeholdElite.class, Island.class})
class ThoughtweftGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Taps all creatures opponents control")
    void tapsOpponentCreatures() {
        harness.addToBattlefield(player2, new SafeholdElite());
        harness.addToBattlefield(player2, new SafeholdElite());
        List<Permanent> p2 = gd.playerBattlefields.get(player2.getId());

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(p2).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Untaps all creatures you control")
    void untapsOwnCreatures() {
        harness.addToBattlefield(player1, new SafeholdElite());
        harness.addToBattlefield(player1, new SafeholdElite());
        List<Permanent> p1 = gd.playerBattlefields.get(player1.getId());
        p1.forEach(Permanent::tap);

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(p1).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not tap your own creatures")
    void doesNotTapOwnCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        opponentBear.tap();

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap opponent's non-creature permanents")
    void doesNotTapOpponentNonCreatures() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveInstant(player1, 0);

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolves with no opposing creatures and still untaps your creatures")
    void untapsOwnCreaturesWithNoOpposingCreatures() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        elite.tap();

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveInstant(player1, 0);

        assertThat(elite.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Thoughtweft Gambit");
    }

    @Test
    @DisplayName("Uses the spell controller even when cast by the nonactive player")
    void nonactivePlayerTapsOpposingCreaturesAndUntapsTheirOwn() {
        Permanent opposingElite = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        Permanent ownElite = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        ownElite.tap();

        harness.setHand(player2, List.of(new ThoughtweftGambit()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0);

        assertThat(opposingElite.isTapped()).isTrue();
        assertThat(ownElite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects creatures present at resolution rather than only at casting")
    void affectsCreaturesAddedBeforeResolution() {
        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castInstant(player1, 0);

        Permanent opposingElite = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        Permanent ownElite = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        ownElite.tap();
        harness.passBothPriorities();

        assertThat(opposingElite.isTapped()).isTrue();
        assertThat(ownElite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap your non-creature permanents")
    void doesNotUntapNonCreatures() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        harness.setHand(player1, List.of(new ThoughtweftGambit()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(island.isTapped()).isTrue();
    }
}
