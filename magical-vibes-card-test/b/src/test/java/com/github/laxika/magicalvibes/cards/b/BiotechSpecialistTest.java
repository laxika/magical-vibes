package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.ImplementsOfSacrifice;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiotechSpecialist.class, ImplementsOfSacrifice.class, Forest.class})
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
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void sacrificingCreatedLanderDealsDamageBeforeSearchingForTappedBasicLand() {
        harness.enterBattlefieldAndReturn(player1, new BiotechSpecialist());
        resolveAllTriggers();
        harness.addToBattlefield(player2, new BiotechSpecialist());
        Forest forest = new Forest();
        BiotechSpecialist nonland = new BiotechSpecialist();
        harness.setLibrary(player1, List.of(nonland, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Lander");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Forest");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void landerSacrificeStillDealsDamageWhenLibraryIsEmpty() {
        harness.enterBattlefieldAndReturn(player1, new BiotechSpecialist());
        resolveAllTriggers();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Lander");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
