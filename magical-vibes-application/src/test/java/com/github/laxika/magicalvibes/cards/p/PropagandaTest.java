package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Propaganda.class, GrizzlyBears.class, ChandraNalaar.class})
class PropagandaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking the Propaganda controller costs {2} per attacker")
    void attackerPaysTwo() {
        harness.addToBattlefield(player1, new Propaganda());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Attack is rejected without enough mana for the tax")
    void cannotAttackWithoutPayment() {
        harness.addToBattlefield(player1, new Propaganda());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Tax is charged for each attacking creature")
    void taxScalesWithAttackerCount() {
        harness.addToBattlefield(player1, new Propaganda());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Propaganda does not tax attacks against its controller's opponent")
    void doesNotTaxAttacksAgainstOthers() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Propaganda());

        declareAttackers(player1, List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Attacking a planeswalker controlled by Propaganda's controller is not taxed")
    void doesNotTaxAttacksAgainstPlaneswalker() {
        harness.addToBattlefield(player1, new Propaganda());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
