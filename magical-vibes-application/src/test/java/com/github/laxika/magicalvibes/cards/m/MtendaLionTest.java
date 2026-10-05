package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MtendaLion.class, BayFalcon.class, InvasionOfZendikar.class})
class MtendaLionTest extends BaseCardTest {

    @Test
    @DisplayName("Defending player pays {U} — no combat damage is dealt by the Lion")
    void defendingPlayerPaysToPreventDamage() {
        addCreatureReady(player1, new MtendaLion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Defending player declines — combat damage is dealt normally")
    void declinedPaymentLetsDamageThrough() {
        addCreatureReady(player1, new MtendaLion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player2, false);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Paid prevention applies only to the Lion's combat damage")
    void paidPreventionOnlyStopsLionsDamage() {
        addCreatureReady(player1, new MtendaLion());
        addCreatureReady(player1, new BayFalcon());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player2, true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Without the mana to pay, the acceptance fails and damage is dealt")
    void acceptingWithoutManaDealsDamage() {
        addCreatureReady(player1, new MtendaLion());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevention also stops damage dealt to a blocking creature")
    void paidPreventionStopsDamageToBlocker() {
        addCreatureReady(player1, new MtendaLion());
        Permanent blocker = addCreatureReady(player2, new BayFalcon());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player2, true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void paymentDoesNotPreventDamageDealtToLion() {
        addCreatureReady(player1, new MtendaLion());
        addCreatureReady(player2, new BayFalcon());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player2, true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Mtenda Lion");
        harness.assertOnBattlefield(player2, "Bay Falcon");
    }

    @Test
    void wrongManaColorCannotPayForPrevention() {
        addCreatureReady(player1, new MtendaLion());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player2, true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void defendingPlayerPaysWhenPlayerTwoAttacks() {
        addCreatureReady(player2, new MtendaLion());
        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @CardUsed(InvasionOfZendikar.class)
    void battleProtectorIsOfferedPaymentRatherThanBattleController() {
        addCreatureReady(player1, new MtendaLion());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }
}
