package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RuthlessCullblade;
import com.github.laxika.magicalvibes.cards.w.WalkingAtlas;
import com.github.laxika.magicalvibes.cards.s.Smother;
import com.github.laxika.magicalvibes.cards.c.ChainReaction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalastriaHighborn.class, RuthlessCullblade.class, WalkingAtlas.class, Smother.class, ChainReaction.class})
class KalastriaHighbornTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B} after another Vampire dies drains the targeted player")
    void payingAfterAnotherVampireDiesDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new KalastriaHighborn());
        harness.addToBattlefield(player1, new RuthlessCullblade());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        killPermanent(player1, "Ruthless Cullblade");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("The ability also triggers when Kalastria Highborn dies")
    void triggersWhenThisCreatureDies() {
        harness.addToBattlefield(player1, new KalastriaHighborn());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        killPermanent(player1, "Kalastria Highborn");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Declining the payment does nothing")
    void decliningPaymentDoesNothing() {
        harness.addToBattlefield(player1, new KalastriaHighborn());
        harness.addToBattlefield(player1, new RuthlessCullblade());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        killPermanent(player1, "Ruthless Cullblade");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A non-Vampire creature dying does not trigger the ability")
    void doesNotTriggerForNonVampire() {
        harness.addToBattlefield(player1, new KalastriaHighborn());
        harness.addToBattlefield(player1, new WalkingAtlas());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        killPermanent(player1, "Walking Atlas");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Vampire dying does not trigger the ability")
    void doesNotTriggerForOpponentsVampire() {
        harness.addToBattlefield(player1, new KalastriaHighborn());
        harness.addToBattlefield(player2, new RuthlessCullblade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        killPermanent(player2, "Ruthless Cullblade");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The controller can target themselves and both life changes happen")
    void canTargetController() {
        harness.addToBattlefield(player1, new KalastriaHighborn());
        harness.addMana(player1, ManaColor.BLACK, 1);

        killPermanent(player1, "Kalastria Highborn");
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Target is chosen before mana is added for the payment")
    void canAddManaAfterChoosingTarget() {
        harness.addToBattlefield(player1, new KalastriaHighborn());

        killPermanent(player1, "Kalastria Highborn");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Simultaneous deaths trigger for Highborn and each other Vampire")
    void simultaneousDeathsProduceSeparatePayments() {
        harness.addToBattlefield(player1, new KalastriaHighborn());
        harness.addToBattlefield(player1, new RuthlessCullblade());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ChainReaction()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player2, 0, (UUID) null);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kalastria Highborn");
        harness.assertInGraveyard(player1, "Ruthless Cullblade");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Without black mana the trigger can be declined without changing life")
    void canDeclineWithoutBlackMana() {
        harness.addToBattlefield(player1, new KalastriaHighborn());

        killPermanent(player1, "Kalastria Highborn");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private void killPermanent(com.github.laxika.magicalvibes.model.Player controller, String name) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Smother()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        UUID permanentId = harness.getPermanentId(controller, name);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }
}
