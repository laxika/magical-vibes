package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.Exsanguinate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolitaryConfinement;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TevalArbiterOfVirtue.class, Shock.class, ThinkTwice.class, Exsanguinate.class,
        SolitaryConfinement.class})
class TevalArbiterOfVirtueTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's spell has delve and costs life equal to its mana value")
    void grantsDelveAndLosesSpellManaValueLife() {
        Card graveyardCard = new Shock();
        harness.addToBattlefield(player1, new TevalArbiterOfVirtue());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardCard);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Teval does not trigger for an opponent's spell")
    void doesNotTriggerForOpponentSpell() {
        harness.addToBattlefield(player1, new TevalArbiterOfVirtue());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int casterLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(casterLifeBefore);
    }

    @Test
    @DisplayName("Life loss includes the chosen X while the spell is on the stack")
    void lifeLossIncludesChosenX() {
        harness.addToBattlefield(player1, new TevalArbiterOfVirtue());
        harness.setHand(player1, List.of(new Exsanguinate()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Delve can pay both generic mana in Think Twice's flashback cost")
    void delvePaysAlternativeFlashbackCost() {
        Card spell = new ThinkTwice();
        Card firstFuel = new ThinkTwice();
        Card secondFuel = new ThinkTwice();
        harness.addToBattlefield(player1, new TevalArbiterOfVirtue());
        harness.setGraveyard(player1, List.of(spell, firstFuel, secondFuel));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromGraveyard(player1, 0, List.of(1, 2));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(firstFuel, secondFuel);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Delve is optional and casting with mana still triggers life loss")
    void canPayManaWithoutDelving() {
        Card fuel = new ThinkTwice();
        harness.addToBattlefield(player1, new TevalArbiterOfVirtue());
        harness.setGraveyard(player1, List.of(fuel));
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fuel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Teval gains life from its combat damage")
    void combatDamageGainsLife() {
        harness.addToBattlefieldAndReturn(player1, new TevalArbiterOfVirtue()).setSummoningSick(false);
        harness.setLife(player1, 10);
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 6);
    }

    @Test
    @DisplayName("Shroud does not stop Teval's non-targeting life loss")
    void shroudDoesNotPreventLifeLoss() {
        harness.addToBattlefield(player1, new TevalArbiterOfVirtue());
        harness.addToBattlefield(player1, new SolitaryConfinement());
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }
}
