package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhoenixChick.class, DoublingSeason.class})
class PhoenixChickTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers from the graveyard when attacking with three creatures")
    void triggersWithThreeAttackers() {
        addThreeReadyChicks();
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
        addCreatureReady(player1, new PhoenixChick());
        addCreatureReady(player1, new PhoenixChick());
        harness.setGraveyard(player1, List.of(new PhoenixChick()));

        declareAttackers(List.of(0, 1));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack.stream().noneMatch(entry -> entry.getCard().getName().equals("Phoenix Chick"))).isTrue();
    }

    @Test
    @DisplayName("Paying {R}{R} returns Phoenix Chick tapped and attacking with a +1/+1 counter")
    void returnsTappedAndAttackingWithCounter() {
        PhoenixChick chick = new PhoenixChick();
        addThreeReadyChicks();
        harness.setGraveyard(player1, List.of(chick));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player1, true));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(chick.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(chick.getId()));
    }

    @Test
    @DisplayName("Phoenix Chick can't block")
    void cantBlock() {
        addCreatureReady(player1, new PhoenixChick());
        addCreatureReady(player2, new PhoenixChick());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining the payment leaves Phoenix Chick in the graveyard")
    void decliningPaymentLeavesCardInGraveyard() {
        addThreeReadyChicks();
        harness.setGraveyard(player1, List.of(new PhoenixChick()));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Phoenix Chick");
        assertThat(countPermanents(player1, "Phoenix Chick")).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent attacking with three creatures does not trigger your graveyard card")
    void opponentsAttackDoesNotTrigger() {
        addCreatureReady(player2, new PhoenixChick());
        addCreatureReady(player2, new PhoenixChick());
        addCreatureReady(player2, new PhoenixChick());
        harness.setGraveyard(player1, List.of(new PhoenixChick()));

        declareAttackers(player2, List.of(0, 1, 2));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Phoenix Chick");
    }

    @Test
    @DisplayName("Each graveyard copy requires a separate payment")
    void payingForOneCopyDoesNotReturnBoth() {
        addThreeReadyChicks();
        harness.setGraveyard(player1, List.of(new PhoenixChick(), new PhoenixChick()));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Phoenix Chick")).isEqualTo(4);
    }

    @Test
    @CardUsed({PhoenixChick.class, DoublingSeason.class})
    @DisplayName("Doubling Season doubles the counter Phoenix Chick enters with")
    void counterReplacementAppliesOnReturn() {
        PhoenixChick chick = new PhoenixChick();
        addThreeReadyChicks();
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setGraveyard(player1, List.of(chick));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(chick.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Leaving and reentering the graveyard makes the old attack trigger lose track of its card")
    void oldTriggerCannotReturnNewGraveyardObject() {
        PhoenixChick chick = new PhoenixChick();
        addThreeReadyChicks();
        harness.setGraveyard(player1, List.of(chick));
        gd.markGraveyardEntry(chick);
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0, 1, 2));
        gd.playerGraveyards.get(player1.getId()).remove(chick);
        harness.setExile(player1, List.of(chick));
        gd.exiledCards.removeIf(entry -> entry.card().getId().equals(chick.getId()));
        harness.setGraveyard(player1, List.of(chick));
        gd.markGraveyardEntry(chick);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Phoenix Chick");
        assertThat(countPermanents(player1, "Phoenix Chick")).isEqualTo(3);
    }

    private void addThreeReadyChicks() {
        addCreatureReady(player1, new PhoenixChick());
        addCreatureReady(player1, new PhoenixChick());
        addCreatureReady(player1, new PhoenixChick());
    }
}
