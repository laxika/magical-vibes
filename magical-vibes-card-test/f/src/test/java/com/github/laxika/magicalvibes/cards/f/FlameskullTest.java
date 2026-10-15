package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BurningHands;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Flameskull.class, BurningHands.class, Mountain.class})
class FlameskullTest extends BaseCardTest {

    @Test
    @DisplayName("Rejuvenation leaves both cards available without choosing during resolution")
    void deathExilesFlameskullAndTopCard() {
        Card topCard = new BurningHands();
        harness.setLibrary(player1, List.of(topCard));
        Permanent flameskull = harness.addToBattlefieldAndReturn(player1, new Flameskull());
        Card flameskullCard = flameskull.getCard();

        destroyFlameskull(flameskull);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(topCard, flameskullCard);
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(flameskullCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exilePlayPermissions)
                .containsEntry(flameskullCard.getId(), player1.getId())
                .containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Playing the library card consumes the permission for both exiled cards")
    void choosingTopCardUsesTheSinglePermission() {
        Card topCard = new Flameskull();
        harness.setLibrary(player1, List.of(topCard));
        Permanent flameskull = harness.addToBattlefieldAndReturn(player1, new Flameskull());
        Card flameskullCard = flameskull.getCard();

        destroyFlameskull(flameskull);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(flameskullCard);
        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, flameskullCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flameskull can be cast after dying with an empty library")
    void emptyLibraryStillAllowsCastingFlameskull() {
        harness.setLibrary(player1, List.of());
        Permanent flameskull = harness.addToBattlefieldAndReturn(player1, new Flameskull());
        Card sourceCard = flameskull.getCard();

        destroyFlameskull(flameskull);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sourceCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, sourceCard.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Flameskull");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejuvenation does not exile the library card if Flameskull has left the graveyard")
    void sourceAbsentFromGraveyardDoesNotExileTopCard() {
        Card topCard = new BurningHands();
        harness.setLibrary(player1, List.of(topCard));
        Permanent flameskull = harness.addToBattlefieldAndReturn(player1, new Flameskull());
        harness.setHand(player1, List.of(new BurningHands()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, flameskull.getId());
        harness.assertInGraveyard(player1, "Flameskull");
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Flameskull cannot block even while untapped")
    void cannotBlock() {
        Permanent flameskull = harness.addToBattlefieldAndReturn(player1, new Flameskull());

        assertThat(bls.canBlock(gd, flameskull)).isFalse();
    }

    @Test
    @DisplayName("Casting Flameskull requires normal mana and timing and consumes the other option")
    void castingSourceRequiresNormalCostsAndTiming() {
        Card topCard = new Flameskull();
        harness.setLibrary(player1, List.of(topCard));
        Permanent flameskull = harness.addToBattlefieldAndReturn(player1, new Flameskull());
        Card sourceCard = flameskull.getCard();
        destroyFlameskull(flameskull);

        assertThatThrownBy(() -> harness.castFromExile(player1, sourceCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.castFromExile(player1, sourceCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, sourceCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(sourceCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Playing the exiled land consumes the option to cast Flameskull")
    void canPlayExiledLandInsteadOfFlameskull() {
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent flameskull = harness.addToBattlefieldAndReturn(player1, new Flameskull());
        Card sourceCard = flameskull.getCard();
        destroyFlameskull(flameskull);

        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sourceCard);
        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, sourceCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void destroyFlameskull(Permanent flameskull) {
        harness.setHand(player1, List.of(new BurningHands()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, flameskull.getId());
        harness.passBothPriorities();
    }
}
