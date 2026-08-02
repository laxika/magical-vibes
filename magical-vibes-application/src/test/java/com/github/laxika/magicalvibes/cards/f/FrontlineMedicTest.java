package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrontlineMedicTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion grants indestructible to creatures you control")
    void battalionGrantsIndestructible() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(otherAttacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Battalion's indestructible grant wears off at end of turn")
    void battalionIndestructibleWearsOff() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Sacrifice ability counters an X spell when its controller cannot pay")
    void countersXSpellWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new FrontlineMedic());

        Blaze blaze = new Blaze();
        harness.setHand(player2, List.of(blaze));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, blaze.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Blaze");
        harness.assertInGraveyard(player1, "Frontline Medic");
    }

    @Test
    @DisplayName("Sacrifice ability lets the controller pay {3} to keep an X spell")
    void XSpellSurvivesWhenControllerPays() {
        harness.addToBattlefield(player1, new FrontlineMedic());

        Blaze blaze = new Blaze();
        harness.setHand(player2, List.of(blaze));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, blaze.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Frontline Medic");
        harness.assertInGraveyard(player2, "Blaze");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a spell without X in its mana cost")
    void cannotTargetNonXSpell() {
        harness.addToBattlefield(player1, new FrontlineMedic());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
