package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaliciousInvader;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InnocentTraveler.class, MaliciousInvader.class, GrizzlyBears.class, TravelingMinister.class})
class InnocentTravelerTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when no opponent sacrifices a creature")
    void transformsWhenNoOpponentSacrifices() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());

        triggerUpkeep(player1);

        assertThat(traveler.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("An opponent who declines causes the Traveler to transform")
    void decliningCausesTransform() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        harness.addToBattlefield(player2, new GrizzlyBears());

        triggerUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(traveler.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent who sacrifices a creature prevents the transform")
    void sacrificingPreventsTransform() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerUpkeep(player1);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(traveler.isTransformed()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Malicious Invader gets its conditional power bonus from an opposing Human")
    void backFaceGetsBonusFromOpposingHuman() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());
        triggerUpkeep(player1);
        int powerWithoutHuman = gqs.getEffectivePower(gd, traveler);

        harness.addToBattlefield(player2, new TravelingMinister());

        assertThat(gqs.getEffectivePower(gd, traveler)).isEqualTo(powerWithoutHuman + 2);
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent traveler = harness.addToBattlefieldAndReturn(player1, new InnocentTraveler());

        advanceToUpkeep(player2);

        assertThat(traveler.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void triggerUpkeep(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }
}
