package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HissingMiasma.class, GhostWarden.class, JaceBeleren.class, Mortify.class})
class HissingMiasmaTest extends BaseCardTest {

    private void setUpAttack(int count) {
        harness.addToBattlefield(player1, new HissingMiasma());

        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new GhostWarden());
        }
    }

    @Test
    @DisplayName("Each attacking creature makes its controller lose 1 life")
    void eachAttackerCausesLifeLoss() {
        setUpAttack(2);
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declaring no attackers produces no trigger")
    void noAttackersNoTrigger() {
        setUpAttack(1);

        declareAttackers(player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Attacking a planeswalker you control does not trigger Hissing Miasma")
    void attackingPlaneswalkerDoesNotTrigger() {
        setUpAttack(1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Multiple copies each trigger for every attacker")
    void multipleCopiesTriggerIndependently() {
        setUpAttack(2);
        harness.addToBattlefield(player1, new HissingMiasma());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(4);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The trigger still causes life loss after the attacker is destroyed")
    void destroyedAttackerStillCausesLifeLoss() {
        setUpAttack(1);
        Permanent attacker = gd.playerBattlefields.get(player2.getId()).getFirst();
        declareAttackers(player2, List.of(0));

        destroyInResponse(attacker);
        harness.assertInGraveyard(player2, "Ghost Warden");
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroying Hissing Miasma does not stop its pending trigger")
    void destroyedMiasmaStillCausesLifeLoss() {
        setUpAttack(1);
        Permanent miasma = findPermanent(player1, "Hissing Miasma");
        declareAttackers(player2, List.of(0));

        destroyInResponse(miasma);
        harness.assertInGraveyard(player1, "Hissing Miasma");
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Your own attacking creatures do not trigger your Hissing Miasma")
    void ownAttackDoesNotTrigger() {
        harness.addToBattlefield(player2, new HissingMiasma());
        addCreatureReady(player2, new GhostWarden());

        declareAttackers(player2, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void destroyInResponse(Permanent permanent) {
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.castAndResolveInstant(player1, 0, permanent.getId()));
    }
}
