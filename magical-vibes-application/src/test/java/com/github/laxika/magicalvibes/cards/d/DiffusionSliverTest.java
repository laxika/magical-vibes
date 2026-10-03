package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiffusionSliver.class, ElaborateFirecannon.class, GrizzlyBears.class, Shock.class, Mutavault.class})
class DiffusionSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent spell targeting a Sliver when its controller cannot pay")
    void countersOpponentSpellTargetingSliver() {
        Permanent sliver = addReadySliver();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, sliver.getId());

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Diffusion Sliver");
        assertThat(sliver.getMarkedDamage()).isZero();
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
        harness.assertInGraveyard(player1, "Diffusion Sliver");
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

    @Test
    @DisplayName("A player can decline the payment even when they have enough mana")
    void countersWhenPaymentIsDeclined() {
        Permanent sliver = addReadySliver();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, sliver.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Diffusion Sliver");
        assertThat(sliver.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Your own spell targeting your Sliver does not trigger the ability")
    void doesNotTriggerForOwnSpell() {
        Permanent sliver = addReadySliver();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, sliver.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Diffusion Sliver");
    }

    @Test
    @DisplayName("An opponent's Sliver is not protected by your Diffusion Sliver")
    void doesNotProtectOpponentsSliver() {
        addReadySliver();
        Permanent opposingSliver = addReadyCreature(player2, new DiffusionSliver());
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, opposingSliver.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Diffusion Sliver");
        harness.assertOnBattlefield(player1, "Diffusion Sliver");
    }

    @Test
    @DisplayName("Each Diffusion Sliver requires a separate payment")
    void multipleSliversRequireSeparatePayments() {
        Permanent sliver = addReadySliver();
        addReadySliver();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, sliver.getId());

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(sliver.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An animated Mutavault is a Sliver creature and receives protection")
    void protectsAnimatedMutavault() {
        addReadySliver();
        Permanent mutavault = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutavault), null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, mutavault.getId());

        harness.assertOnBattlefield(player1, "Mutavault");
        assertThat(mutavault.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    private Permanent addReadySliver() {
        return addReadyCreature(player1, new DiffusionSliver());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyFirecannon() {
        return addReadyCreature(player2, new ElaborateFirecannon());
    }

    private void beginOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
