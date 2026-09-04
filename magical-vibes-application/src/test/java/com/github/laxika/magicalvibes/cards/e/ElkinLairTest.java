package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Fireblast;
import com.github.laxika.magicalvibes.cards.j.JamuraanLion;
import com.github.laxika.magicalvibes.cards.u.UndiscoveredParadise;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextEndStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElkinLair.class, Fireblast.class, JamuraanLion.class, UndiscoveredParadise.class})
class ElkinLairTest extends BaseCardTest {

    @Test
    @DisplayName("Active player's upkeep exiles a random hand card with this-turn play permission")
    void upkeepExilesRandomHandCardWithPlayPermission() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card only = new Fireblast();
        harness.setHand(player1, List.of(only));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.exilePlayPermissions.get(only.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(only.getId());

        List<ExileToOwnerGraveyardAtNextEndStep> scheduled =
                gd.getDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().cardId()).isEqualTo(only.getId());
        assertThat(scheduled.getFirst().ownerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exiles exactly one card from a hand with multiple cards")
    void exilesExactlyOneCardFromMultipleCardHand() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card first = new Fireblast();
        Card second = new JamuraanLion();
        harness.setHand(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()).getFirst().getId())
                .isIn(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Opponent's upkeep also exiles from that player's hand")
    void opponentUpkeepExilesFromOpponentHand() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card only = new JamuraanLion();
        harness.setHand(player2, List.of(only));
        harness.setHand(player1, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.exilePlayPermissions.get(only.getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Only the affected player may play the exiled card")
    void onlyAffectedPlayerMayPlayExiledCard() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card only = new UndiscoveredParadise();
        harness.setHand(player2, List.of(only));
        harness.setHand(player1, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, only.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(only);
    }

    @Test
    @DisplayName("Empty hand is a no-op")
    void emptyHandDoesNothing() {
        harness.addToBattlefield(player1, new ElkinLair());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("Unplayed card goes to graveyard at next end step")
    void unplayedCardGoesToGraveyardAtEndStep() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card only = new Fireblast();
        harness.setHand(player1, List.of(only));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(only.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("A played card is not put into the graveyard at end step")
    void playedCardIsNotPutIntoGraveyard() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card land = new UndiscoveredParadise();
        harness.setHand(player1, List.of(land));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        gs.playCardFromExile(gd, player1, land.getId(), null, null);
        harness.assertOnBattlefield(player1, "Undiscovered Paradise");

        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
        assertThat(gd.playerGraveyards.getOrDefault(player1.getId(), List.of()))
                .noneMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("An instant can be played in response to the delayed graveyard trigger")
    void canPlayInstantInResponseToDelayedGraveyardTrigger() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card fireblast = new Fireblast();
        harness.setHand(player1, List.of(fireblast));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player2, new JamuraanLion());
        var lion = findPermanent(player2, "Jamuraan Lion");

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(fireblast);
        harness.castFromExile(player1, fireblast.getId(), lion.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jamuraan Lion");
        harness.assertInGraveyard(player1, "Fireblast");
    }

    @Test
    @DisplayName("Exiled spell can be cast this turn for its mana cost")
    void mayCastExiledSpellThisTurn() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card fireblast = new Fireblast();
        harness.setHand(player1, List.of(fireblast));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player2, new JamuraanLion());
        var lion = findPermanent(player2, "Jamuraan Lion");

        gs.playCardFromExile(gd, player1, fireblast.getId(), null, lion.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Jamuraan Lion");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(fireblast.getId()));
    }
}
