package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent target = addCreatureReady(player2, new FugitiveWizard());

        tap(medics);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Tapping another permanent you control does not trigger")
    void tappingAnotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new GoblinMedics());
        Permanent otherCreature = addCreatureReady(player1, new FugitiveWizard());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
