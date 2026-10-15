package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BarteredCow;
import com.github.laxika.magicalvibes.cards.h.HengeWalker;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlassCasket.class, YouthfulKnight.class, BarteredCow.class, HengeWalker.class, ReturnToNature.class})
class GlassCasketTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opponent creature with mana value 3 or less")
    void etbExilesSmallOpponentCreature() {
        UUID knightId = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight()).getId();

        castAndResolve(knightId);

        harness.assertNotOnBattlefield(player2, "Youthful Knight");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Youthful Knight"));
    }

    @Test
    @DisplayName("Cannot target an opponent creature with mana value greater than 3")
    void cannotTargetLargeOpponentCreature() {
        UUID cowId = harness.addToBattlefieldAndReturn(player2, new BarteredCow()).getId();
        UUID legalId = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight()).getId();
        castUntilTargetChoice();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, cowId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Bartered Cow");
        harness.assertNotOnBattlefield(player2, "Youthful Knight");
    }

    @Test
    @DisplayName("Exiled creature returns when Glass Casket leaves the battlefield")
    void exiledCreatureReturnsWhenSourceLeaves() {
        UUID knightId = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight()).getId();
        castAndResolve(knightId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID casketId = harness.getPermanentId(player1, "Glass Casket");
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, casketId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Youthful Knight");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Youthful Knight"));
    }

    @Test
    @DisplayName("Can exile an artifact creature with mana value exactly three")
    void exilesCreatureAtManaValueLimit() {
        UUID walkerId = harness.addToBattlefieldAndReturn(player2, new HengeWalker()).getId();

        castAndResolve(walkerId);

        harness.assertNotOnBattlefield(player2, "Henge Walker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Henge Walker"));
    }

    @Test
    @DisplayName("Cannot target a creature controlled by Glass Casket's controller")
    void cannotTargetOwnCreature() {
        UUID ownId = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight()).getId();
        UUID opponentId = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight()).getId();
        castUntilTargetChoice();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player2, "Youthful Knight");
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact even with mana value less than three")
    void cannotTargetNoncreatureArtifact() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new GlassCasket()).getId();
        UUID knightId = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight()).getId();
        castUntilTargetChoice();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifactId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, knightId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glass Casket");
        harness.assertNotOnBattlefield(player2, "Youthful Knight");
    }

    @Test
    @DisplayName("Glass Casket can resolve without a legal creature to exile")
    void resolvesWithoutLegalTargets() {
        harness.addToBattlefield(player2, new BarteredCow());
        harness.addToBattlefield(player1, new YouthfulKnight());

        castUntilTargetChoice();

        harness.assertOnBattlefield(player1, "Glass Casket");
        harness.assertOnBattlefield(player1, "Youthful Knight");
        harness.assertOnBattlefield(player2, "Bartered Cow");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaving before the enters trigger resolves prevents exile")
    void sourceLeavesBeforeTriggerResolves() {
        UUID knightId = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight()).getId();
        castUntilTargetChoice();
        harness.handlePermanentChosen(player1, knightId);

        UUID casketId = harness.getPermanentId(player1, "Glass Casket");
        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, 0, casketId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Glass Casket");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Youthful Knight");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature removed in response to the trigger is not exiled")
    void targetLeavesBeforeTriggerResolves() {
        UUID walkerId = harness.addToBattlefieldAndReturn(player2, new HengeWalker()).getId();
        castUntilTargetChoice();
        harness.handlePermanentChosen(player1, walkerId);

        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, 0, walkerId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glass Casket");
        harness.assertInGraveyard(player2, "Henge Walker");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castAndResolve(UUID targetId) {
        castUntilTargetChoice();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    private void castUntilTargetChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GlassCasket(), "{1}{W}");
        harness.passBothPriorities();
    }
}
