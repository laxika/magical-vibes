package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhoenixChick.class, GrizzlyBears.class})
class PhoenixChickTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers from the graveyard when attacking with three creatures")
    void triggersWithThreeAttackers() {
        addThreeReadyBears();
        harness.setGraveyard(player1, List.of(new PhoenixChick()));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{R}{R}");
    }

    @Test
    @DisplayName("Does not trigger from the graveyard when attacking with fewer than three creatures")
    void doesNotTriggerWithFewerThanThreeAttackers() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new PhoenixChick()));

        declareAttackers(List.of(0, 1));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack.stream().noneMatch(entry -> entry.getCard().getName().equals("Phoenix Chick"))).isTrue();
    }

    @Test
    @DisplayName("Paying {R}{R} returns Phoenix Chick tapped and attacking with a +1/+1 counter")
    void returnsTappedAndAttackingWithCounter() {
        PhoenixChick chick = new PhoenixChick();
        addThreeReadyBears();
        harness.setGraveyard(player1, List.of(chick));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(chick.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(chick.getId()));
    }

    @Test
    @DisplayName("Phoenix Chick can't block")
    void cantBlock() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new PhoenixChick());

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addThreeReadyBears() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
    }
}
