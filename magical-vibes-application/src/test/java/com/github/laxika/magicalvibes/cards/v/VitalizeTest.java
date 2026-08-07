package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VitalizeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all tapped creatures you control")
    void untapsAllTappedCreaturesYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent bear1 = battlefield.get(0);
        bear1.tap();
        Permanent bear2 = battlefield.get(1);
        bear2.tap();

        harness.setHand(player1, List.of(new Vitalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps every creature without prompting for a choice")
    void untapsWithoutPromptingForAChoice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).forEach(Permanent::tap);

        harness.setHand(player1, List.of(new Vitalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // "Untap all creatures you control" has no chosenCount, so it must not take the
        // "untap up to N" branch that Rewind and Unwind share with it.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).getFirst();
        opponentBear.tap();

        harness.setHand(player1, List.of(new Vitalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap non-creature permanents you control")
    void doesNotUntapNonCreaturePermanents() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = gd.playerBattlefields.get(player1.getId()).getFirst();
        island.tap();

        harness.setHand(player1, List.of(new Vitalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(island.isTapped()).isTrue();
    }
}
