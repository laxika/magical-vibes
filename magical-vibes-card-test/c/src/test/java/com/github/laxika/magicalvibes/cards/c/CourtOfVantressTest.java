package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CourtOfVantress.class, SolRing.class, GrizzlyBears.class})
class CourtOfVantressTest extends BaseCardTest {

    @Test
    void becomesMonarchWhenItEnters() {
        harness.enterBattlefieldAndReturn(player1, new CourtOfVantress());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void monarchCreatesTokenCopyOfTargetArtifactOrEnchantment() {
        Permanent court = harness.enterBattlefieldAndReturn(player1, new CourtOfVantress());
        resolveAllTriggers();
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ring.getId(), player1.getId());

        harness.handlePermanentChosen(player1, ring.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(court.getCard().getName()).isEqualTo("Court of Vantress");
        assertThat(countPermanents(player1, "Sol Ring")).isEqualTo(2);
    }

    @Test
    void nonmonarchBecomesCopyAndRetainsItsAbility() {
        Permanent court = harness.enterBattlefieldAndReturn(player1, new CourtOfVantress());
        resolveAllTriggers();
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.monarchPlayerId = player2.getId();

        advanceToUpkeep(player1);
        chooseAndAcceptTarget(ring);

        assertThat(court.getCard().getName()).isEqualTo("Sol Ring");
        assertThat(court.getCard().getTargetFilter()).isNotNull();

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.validIds()).containsExactlyInAnyOrder(ring.getId(), player1.getId());
    }

    private void chooseAndAcceptTarget(Permanent target) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
