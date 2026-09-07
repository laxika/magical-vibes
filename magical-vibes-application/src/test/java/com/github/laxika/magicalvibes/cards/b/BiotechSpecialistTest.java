package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.ImplementsOfSacrifice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiotechSpecialist.class, ImplementsOfSacrifice.class})
class BiotechSpecialistTest extends BaseCardTest {

    @Test
    void createsLanderOnEnter() {
        harness.enterBattlefieldAndReturn(player1, new BiotechSpecialist());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    void sacrificingAnArtifactDealsTwoDamageToTargetOpponent() {
        harness.addToBattlefield(player1, new BiotechSpecialist());
        harness.addToBattlefield(player1, new ImplementsOfSacrifice());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
