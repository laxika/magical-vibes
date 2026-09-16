package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EscapeArtist.class, AngelicWall.class})
class EscapeArtistTest extends BaseCardTest {

    @Test
    @DisplayName("Escape Artist cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = addCreatureReady(player2, new AngelicWall());

        Permanent artist = addCreatureReady(player1, new EscapeArtist());
        artist.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(artist);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Paying {U} and discarding a card returns Escape Artist to its owner's hand")
    void activateAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new EscapeArtist());
        harness.setHand(player1, List.of(new AngelicWall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Escape Artist");
        harness.assertNotOnBattlefield(player1, "Escape Artist");
        harness.assertInGraveyard(player1, "Angelic Wall");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        harness.addToBattlefield(player1, new EscapeArtist());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding a card is paid before Escape Artist's return ability resolves")
    void discardCostIsPaidBeforeAbilityResolves() {
        harness.addToBattlefield(player1, new EscapeArtist());
        harness.setHand(player1, List.of(new AngelicWall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Escape Artist");
        harness.assertInGraveyard(player1, "Angelic Wall");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Escape Artist");
        harness.assertNotOnBattlefield(player1, "Escape Artist");
    }

    @Test
    @DisplayName("Cannot activate the return ability without blue mana")
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new EscapeArtist());
        harness.setHand(player1, List.of(new AngelicWall()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Escape Artist");
        harness.assertInHand(player1, "Angelic Wall");
    }

    @Test
    @DisplayName("Returns Escape Artist to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        EscapeArtist artistCard = new EscapeArtist();
        artistCard.setOwnerId(player1.getId());
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(artistCard));
        harness.setHand(player2, List.of(new AngelicWall()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Escape Artist");
        harness.assertNotInHand(player2, "Escape Artist");
        harness.assertNotOnBattlefield(player2, "Escape Artist");
        harness.assertInGraveyard(player2, "Angelic Wall");
    }
}
