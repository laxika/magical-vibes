package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReplicationTechnique.class, GrizzlyBears.class, SolRing.class, JaceBeleren.class})
class ReplicationTechniqueTest extends BaseCardTest {

    @Test
    void demonstrateMayBeDeclined() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReplicationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    void demonstrateCreatesCopiesForControllerAndChosenOpponent() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReplicationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2)
                .extracting(StackEntry::getControllerId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    void createsTokenCopyOfTargetPermanentYouControl() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReplicationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, bears.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    void demonstrateOffersControllerNewTargetsBeforeOpponentCopies() {
        var ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player2, new SolRing());
        harness.setHand(player1, List.of(new ReplicationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, ring.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .noneMatch(entry -> entry.getControllerId().equals(player2.getId()));
    }

    @Test
    void canCopyANoncreaturePermanent() {
        var ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        ring.tap();
        harness.setHand(player1, List.of(new ReplicationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, ring.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Sol Ring");
                    assertThat(token.isTapped()).isFalse();
                });
    }

    @Test
    void cannotTargetAnOpponentsPermanent() {
        var ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new ReplicationTechnique()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ring.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void planeswalkerCopyEntersWithPrintedStartingLoyalty() {
        var jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new ReplicationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, jace.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token ->
                        assertThat(token.getCounterCount(CounterType.LOYALTY)).isEqualTo(3));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
