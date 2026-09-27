package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NegativeZonePortal.class, GrizzlyBears.class, Shock.class})
class NegativeZonePortalTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's graveyard card, tracks it, and draws for a creature")
    void exilesAndDrawsForCreature() {
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new NegativeZonePortal());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setLibrary(player1, List.of(new Shock()));
        activate(portal, creature);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.getCardsExiledByPermanent(portal.getId())).contains(creature);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Does not draw for a noncreature card")
    void doesNotDrawForNoncreature() {
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new NegativeZonePortal());
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(noncreature));
        harness.setLibrary(player1, List.of(new Shock()));
        int handSize = gd.playerHands.get(player1.getId()).size();
        activate(portal, noncreature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.getCardsExiledByPermanent(portal.getId())).containsExactly(noncreature);
    }

    @Test
    @DisplayName("Cannot target a card in its controller's graveyard")
    void cannotTargetOwnGraveyard() {
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new NegativeZonePortal());
        Card card = new Shock();
        harness.setGraveyard(player1, List.of(card));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, card.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(portal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The upkeep trigger does not flip before four creature cards are exiled")
    void upkeepRequiresFourCreatureCards() {
        setupWithExiledCreatures(3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gameLogContains("coin flip for Negative Zone Portal")).isFalse();
        harness.assertOnBattlefield(player1, "Negative Zone Portal");
    }

    @Test
    @DisplayName("The upkeep trigger flips at four creature cards and loses by sacrificing and returning one")
    void upkeepFlipsAtFourCreatureCards() {
        Permanent portal = setupWithExiledCreatures(4);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip for Negative Zone Portal");
        boolean lost = gameLogContains("loses the coin flip for Negative Zone Portal");
        assertThat(won ^ lost).isTrue();
        if (won) {
            harness.assertOnBattlefield(player1, "Negative Zone Portal");
            assertThat(gd.getCardsExiledByPermanent(portal.getId())).hasSize(4);
        } else {
            harness.assertInGraveyard(player1, "Negative Zone Portal");
            assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        }
    }

    private void activate(Permanent portal, Card target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
    }

    private Permanent setupWithExiledCreatures(int count) {
        Permanent portal = harness.addToBattlefieldAndReturn(player1, new NegativeZonePortal());
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            graveyard.add(new GrizzlyBears());
        }
        harness.setGraveyard(player2, graveyard);

        for (Card creature : graveyard) {
            portal.untap();
            activate(portal, creature);
        }
        return portal;
    }
}
