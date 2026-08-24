package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecraSanctuary.class, GrizzlyBears.class, SavannahLions.class})
class NecraSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses 1 life when you control a green permanent only")
    void losesOneLifeWithGreenPermanentOnly() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());

        resolveUpkeepTargetingPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Target player loses 1 life when you control a white permanent only")
    void losesOneLifeWithWhitePermanentOnly() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new SavannahLions());

        resolveUpkeepTargetingPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Target player loses 3 life when you control green and white permanents")
    void losesThreeLifeWithGreenAndWhitePermanents() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SavannahLions());

        resolveUpkeepTargetingPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger without a green or white permanent")
    void doesNotTriggerWithoutGreenOrWhitePermanent() {
        harness.addToBattlefield(player1, new NecraSanctuary());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Allows targeting either player")
    void allowsTargetingEitherPlayer() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    private void resolveUpkeepTargetingPlayer2() {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
