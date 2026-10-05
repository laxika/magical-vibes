package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SternDismissal;
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

@CardUsed({NessianBoar.class, GrizzlyBears.class, NyxbornColossus.class, SternDismissal.class})
class NessianBoarTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Nessian Boar")
    void allAbleCreaturesMustBlock() {
        Permanent boar = addAttackingCreature(player1, new NessianBoar());
        Permanent blocker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker2 = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gqs.isBlockedByAnyCreature(gd, boar)).isTrue();
        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Each blocking creature's controller draws a card")
    void eachBlockingCreatureControllerDraws() {
        addAttackingCreature(player1, new NessianBoar());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        int player1HandSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSizeBefore);
    }

    @Test
    @DisplayName("Tapped creatures are not required to block Nessian Boar")
    void tappedCreaturesAreNotRequiredToBlock() {
        addAttackingCreature(player1, new NessianBoar());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent tappedBlocker = addCreatureReady(player2, new GrizzlyBears());
        tappedBlocker.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(tappedBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A blocker returning to hand does not prevent its controller drawing")
    void blockerLeavingBeforeResolutionStillDraws() {
        addAttackingCreature(player1, new NessianBoar());
        Permanent blocker = addCreatureReady(player2, new NyxbornColossus());
        harness.setHand(player1, List.of(new SternDismissal()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new NyxbornColossus()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Nyxborn Colossus");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature can satisfy either of two attacking Boars' block requirements")
    void blockerCanChooseBetweenTwoBoars() {
        Permanent first = addAttackingCreature(player1, new NessianBoar());
        Permanent second = addAttackingCreature(player1, new NessianBoar());
        Permanent blocker = addCreatureReady(player2, new NyxbornColossus());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new NyxbornColossus()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gqs.isBlockedByAnyCreature(gd, first)).isFalse();
        assertThat(gqs.isBlockedByAnyCreature(gd, second)).isTrue();
        assertThat(blocker.isBlocking()).isTrue();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Summoning sickness does not excuse an able creature from blocking")
    void summoningSickCreatureMustBlock() {
        addAttackingCreature(player1, new NessianBoar());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        blocker.setSummoningSick(true);

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttackingCreature(com.github.laxika.magicalvibes.model.Player player,
                                           com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
