package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RingOfGix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinMedics.class, GoblinWelder.class, RingOfGix.class})
class GoblinMedicsTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped deals 1 damage to a chosen player")
    void becomingTappedDealsDamageToPlayer() {
        Permanent medics = addCreatureReady(player1, new GoblinMedics());
        harness.setLife(player2, 20);

        tap(medics);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Becoming tapped can deal 1 damage to a creature")
    void becomingTappedDealsDamageToCreature() {
        Permanent medics = addCreatureReady(player1, new GoblinMedics());
        Permanent target = addCreatureReady(player2, new GoblinWelder());

        tap(medics);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Welder");
    }

    @Test
    @DisplayName("Tapping another permanent you control does not trigger")
    void tappingAnotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new GoblinMedics());
        Permanent otherCreature = addCreatureReady(player1, new GoblinWelder());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tapping an opponent's permanent does not trigger")
    void tappingOpponentsPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new GoblinMedics());
        Permanent opponentCreature = addCreatureReady(player2, new GoblinWelder());

        tap(opponentCreature);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the tapped copy triggers when two Medics are controlled")
    void onlyTappedCopyTriggers() {
        Permanent medics = addCreatureReady(player1, new GoblinMedics());
        addCreatureReady(player1, new GoblinMedics());

        tap(medics);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's tap effect triggers Medics even while summoning sick")
    void opponentsTapEffectTriggers() {
        Permanent medics = harness.addToBattlefieldAndReturn(player1, new GoblinMedics());
        harness.addToBattlefield(player2, new RingOfGix());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, medics.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(medics.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A tap effect on already tapped Medics does not trigger")
    void alreadyTappedDoesNotTrigger() {
        Permanent medics = harness.addToBattlefieldAndReturn(player1, new GoblinMedics());
        medics.tap();
        harness.addToBattlefield(player2, new RingOfGix());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, medics.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Medics can target itself with its triggered damage")
    void canTargetItself() {
        Permanent medics = addCreatureReady(player1, new GoblinMedics());

        tap(medics);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, medics.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Medics");
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
