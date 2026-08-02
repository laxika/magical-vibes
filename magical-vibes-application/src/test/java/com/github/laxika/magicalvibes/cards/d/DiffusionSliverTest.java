package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiffusionSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent spell targeting a Sliver when its controller cannot pay")
    void countersOpponentSpellTargetingSliver() {
        Permanent sliver = addReadySliver();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sliver.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Does not counter when the targeted spell's controller pays")
    void doesNotCounterWhenControllerPays() {
        Permanent sliver = addReadySliver();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, sliver.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Does not trigger for a non-Sliver creature")
    void doesNotTriggerForNonSliver() {
        harness.addToBattlefield(player1, new DiffusionSliver());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Counters an opponent ability targeting a Sliver when its controller cannot pay")
    void countersOpponentAbilityTargetingSliver() {
        Permanent sliver = addReadySliver();
        Permanent firecannon = addReadyFirecannon();
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(firecannon), null,
                sliver.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(sliver.getMarkedDamage()).isZero();
    }

    private Permanent addReadySliver() {
        Permanent sliver = new Permanent(new DiffusionSliver());
        sliver.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sliver);
        return sliver;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyFirecannon() {
        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);
        return firecannon;
    }

    private void beginOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
