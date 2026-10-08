package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreeshakerChimera.class, GrizzlyBears.class, WrathOfGod.class})
class TreeshakerChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Treeshaker Chimera")
    void allAbleCreaturesMustBlock() {
        Permanent chimera = addCreatureReady(player1, new TreeshakerChimera());
        chimera.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("When Treeshaker Chimera dies, its controller draws three cards")
    void drawsThreeCardsWhenItDies() {
        harness.addToBattlefield(player1, new TreeshakerChimera());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    @DisplayName("Tapped defenders are exempt, but summoning-sick defenders must block")
    void onlyAbleDefendersMustBlock() {
        Permanent chimera = addCreatureReady(player1, new TreeshakerChimera());
        chimera.setAttacking(true);
        Permanent tapped = addCreatureReady(player2, new GrizzlyBears());
        tapped.tap();
        Permanent newlyEntered = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        newlyEntered.setSummoningSick(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(tapped.isBlocking()).isFalse();
        assertThat(newlyEntered.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An opposing Chimera's death draws cards for its controller")
    void opposingControllerDrawsOnDeath() {
        harness.addToBattlefield(player2, new TreeshakerChimera());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third));

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second, third);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
