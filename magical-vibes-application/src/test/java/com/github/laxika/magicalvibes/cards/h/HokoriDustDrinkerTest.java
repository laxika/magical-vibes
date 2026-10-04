package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({HokoriDustDrinker.class, Forest.class, GrizzlyBears.class})
class HokoriDustDrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped lands stay tapped through the untap step while creatures untap normally")
    void landsDontUntap() {
        harness.addToBattlefield(player1, new HokoriDustDrinker());
        Permanent land = addTapped(player1, new Forest());
        Permanent creature = addTapped(player1, new GrizzlyBears());

        harness.performUntapStep(player1);

        assertThat(land.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The active player chooses one of their own lands to untap at their upkeep")
    void activePlayerUntapsOwnLand() {
        harness.addToBattlefield(player1, new HokoriDustDrinker());
        Permanent landA = addTapped(player1, new Forest());
        Permanent landB = addTapped(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(landA.getId()));

        assertThat(landA.isTapped()).isFalse();
        assertThat(landB.isTapped()).isTrue();
    }

    @Test
    @DisplayName("At an opponent's upkeep that opponent chooses, and only their own lands are offered")
    void opponentChoosesFromTheirOwnLands() {
        harness.addToBattlefield(player1, new HokoriDustDrinker());
        Permanent ownLand = addTapped(player1, new Forest());
        Permanent enemyLand = addTapped(player2, new Forest());

        advanceToUpkeep(player2);
        assertThat(enemyLand.isTapped()).isTrue();
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(enemyLand.getId()).doesNotContain(ownLand.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(enemyLand.getId()));

        assertThat(enemyLand.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("With no lands on the battlefield the upkeep trigger resolves as a no-op")
    void noLandsIsANoOp() {
        harness.addToBattlefield(player1, new HokoriDustDrinker());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The active player may choose an already untapped land and leave a tapped land tapped")
    void canChooseAnUntappedLand() {
        harness.addToBattlefield(player1, new HokoriDustDrinker());
        Permanent untappedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent tappedLand = addTapped(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(untappedLand.getId()));

        assertThat(untappedLand.isTapped()).isFalse();
        assertThat(tappedLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent with no lands cannot untap the Hokori controller's land")
    void opponentWithNoLandsHasNothingToUntap() {
        harness.addToBattlefield(player1, new HokoriDustDrinker());
        Permanent land = addTapped(player1, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An upkeep trigger still untaps a land after Hokori leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        Permanent hokori = harness.addToBattlefieldAndReturn(player1, new HokoriDustDrinker());
        Permanent land = addTapped(player2, new Forest());

        advanceToUpkeep(player2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hokori));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(land.getId()));

        assertThat(land.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Hokori, Dust Drinker");
    }

    @Test
    @DisplayName("Lands untap normally once Hokori leaves the battlefield")
    void untapLockEndsWhenSourceLeaves() {
        Permanent hokori = harness.addToBattlefieldAndReturn(player1, new HokoriDustDrinker());
        Permanent land = addTapped(player2, new Forest());

        harness.performUntapStep(player2);
        assertThat(land.isTapped()).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, hokori));
        harness.performUntapStep(player2);

        assertThat(land.isTapped()).isFalse();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

}
