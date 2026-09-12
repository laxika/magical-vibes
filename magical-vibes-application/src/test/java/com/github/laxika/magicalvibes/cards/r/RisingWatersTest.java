package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RisingWaters.class, KorHaven.class, SpinelessThug.class})
class RisingWatersTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped lands stay tapped through the untap step while creatures untap normally")
    void landsDontUntap() {
        harness.addToBattlefield(player1, new RisingWaters());
        Permanent land = addTapped(player1, new KorHaven());
        Permanent creature = addTapped(player1, new SpinelessThug());

        advanceToUpkeep(player1);

        assertThat(land.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The active player chooses one of their own lands to untap at their upkeep")
    void activePlayerUntapsOwnLand() {
        harness.addToBattlefield(player1, new RisingWaters());
        Permanent landA = addTapped(player1, new KorHaven());
        Permanent landB = addTapped(player1, new KorHaven());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(landA.getId()));

        assertThat(landA.isTapped()).isFalse();
        assertThat(landB.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At an opponent's upkeep only that opponent's lands are offered")
    void opponentChoosesFromTheirOwnLands() {
        harness.addToBattlefield(player1, new RisingWaters());
        Permanent ownLand = addTapped(player1, new KorHaven());
        Permanent enemyLand = addTapped(player2, new KorHaven());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(enemyLand.getId()).doesNotContain(ownLand.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(enemyLand.getId()));

        assertThat(enemyLand.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The upkeep choice offers lands but not creatures")
    void upkeepChoiceOffersOnlyLands() {
        harness.addToBattlefield(player1, new RisingWaters());
        Permanent land = addTapped(player1, new KorHaven());
        Permanent creature = addTapped(player1, new SpinelessThug());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(land.getId()).doesNotContain(creature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));

        assertThat(land.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Lands controlled by another player also stay tapped during their untap step")
    void opponentsLandsAlsoDontUntap() {
        harness.addToBattlefield(player1, new RisingWaters());
        Permanent opponentLand = addTapped(player2, new KorHaven());

        advanceToUpkeep(player2);
        assertThat(opponentLand.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentLand.getId()));

        assertThat(opponentLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With no lands on the battlefield the upkeep trigger resolves as a no-op")
    void noLandsIsANoOp() {
        harness.addToBattlefield(player1, new RisingWaters());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }
}
