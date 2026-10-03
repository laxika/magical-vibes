package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.ScabClanCharger;
import com.github.laxika.magicalvibes.cards.d.DomriRade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BomberCorps.class, ScabClanCharger.class, DomriRade.class})
class BomberCorpsTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion deals 1 damage to target player")
    void battalionDamagesTargetPlayer() {
        addCreatureReady(player1, new BomberCorps());
        addCreatureReady(player1, new ScabClanCharger());
        addCreatureReady(player1, new ScabClanCharger());

        declareAttackers(player1, List.of(0, 1, 2));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        // 1 from the trigger, then 1 + 2 + 2 unblocked combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Battalion deals 1 damage to target creature")
    void battalionDamagesTargetCreature() {
        addCreatureReady(player1, new BomberCorps());
        addCreatureReady(player1, new ScabClanCharger());
        addCreatureReady(player1, new ScabClanCharger());
        Permanent opposing = addCreatureReady(player2, new ScabClanCharger());

        declareAttackers(player1, List.of(0, 1, 2));

        harness.handlePermanentChosen(player1, opposing.getId());
        resolveAllTriggers();

        assertThat(opposing.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void noTriggerWithTooFewAttackers() {
        addCreatureReady(player1, new BomberCorps());
        addCreatureReady(player1, new ScabClanCharger());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Only the 1 + 2 unblocked combat damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Battalion can target Bomber Corps itself")
    void battalionCanDamageItself() {
        Permanent bomber = addCreatureReady(player1, new BomberCorps());
        addCreatureReady(player1, new ScabClanCharger());
        addCreatureReady(player1, new ScabClanCharger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            harness.handlePermanentChosen(player1, bomber.getId());
            resolveAllTriggers();
            assertThat(bomber.getMarkedDamage()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Battalion can target its controller")
    void battalionCanDamageController() {
        addCreatureReady(player1, new BomberCorps());
        addCreatureReady(player1, new ScabClanCharger());
        addCreatureReady(player1, new ScabClanCharger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            harness.handlePermanentChosen(player1, player1.getId());
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        });
    }

    @Test
    @DisplayName("Battalion still resolves after the other attackers leave")
    void battalionUsesAttackEventCount() {
        addCreatureReady(player1, new BomberCorps());
        Permanent first = addCreatureReady(player1, new ScabClanCharger());
        Permanent second = addCreatureReady(player1, new ScabClanCharger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            harness.handlePermanentChosen(player1, player2.getId());
            gd.playerBattlefields.get(player1.getId()).removeAll(List.of(first, second));
            gd.playerGraveyards.get(player1.getId()).add(first.getCard());
            gd.playerGraveyards.get(player1.getId()).add(second.getCard());
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        });
    }

    @Test
    @DisplayName("Battalion does not trigger when Bomber Corps stays behind")
    void bomberMustBeAnAttacker() {
        addCreatureReady(player1, new BomberCorps());
        addCreatureReady(player1, new ScabClanCharger());
        addCreatureReady(player1, new ScabClanCharger());
        addCreatureReady(player1, new ScabClanCharger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1, 2, 3));
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        });
    }

    @Test
    @DisplayName("Battalion deals damage to a planeswalker")
    void battalionDamagesPlaneswalker() {
        addCreatureReady(player1, new BomberCorps());
        addCreatureReady(player1, new ScabClanCharger());
        addCreatureReady(player1, new ScabClanCharger());
        Permanent domri = harness.addToBattlefieldAndReturn(player2, new DomriRade());
        domri.setCounterCount(CounterType.LOYALTY, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            harness.handlePermanentChosen(player1, domri.getId());
            resolveAllTriggers();
            assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            harness.assertLife(player2, 20);
        });
    }
}
