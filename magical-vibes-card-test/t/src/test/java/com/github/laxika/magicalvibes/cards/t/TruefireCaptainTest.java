package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TruefireCaptain.class, FugitiveWizard.class, JaceBeleren.class, Shock.class})
class TruefireCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsOnlyAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new TruefireCaptain());
        Permanent attackingWizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent nonAttackingWizard = addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attackingWizard.getId());

        harness.handlePermanentChosen(player1, attackingWizard.getId());
        resolveAllTriggers();

        assertThat(attackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Deals the damage received to a target player")
    void dealsDamageReceivedToTargetPlayer() {
        Permanent captain = addCreatureReady(player2, new TruefireCaptain());
        harness.addToBattlefield(player1, new JaceBeleren());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, captain.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(captain.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void combatDamageOffersOnlyPlayersAndDealsTheDamageReceived() {
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent captain = addCreatureReady(player2, new TruefireCaptain());
        wizard.setAttacking(true);
        captain.setBlocking(true);
        captain.addBlockingTarget(0);

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).isEmpty();
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    void damageTriggerCanTargetItsControllerWithoutPlaneswalkers() {
        Permanent captain = addCreatureReady(player2, new TruefireCaptain());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, captain.getId());
        harness.handlePermanentChosen(player2, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void lethalDamageStillTriggersAndUsesTheLastDamageEventAmount() {
        Permanent captain = addCreatureReady(player2, new TruefireCaptain());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, captain.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player1, 0, captain.getId());
        harness.assertInGraveyard(player2, "Truefire Captain");
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 16);
    }

    @Test
    void mentorDoesNotAddACounterWhenTargetNoLongerHasLesserPower() {
        addCreatureReady(player1, new TruefireCaptain());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, wizard.getId());
        wizard.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

}
