package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RabarooTroop.class, Forest.class, Plains.class})
class RabarooTroopTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall grants flying until end of turn and gains 1 life")
    void landfallGrantsFlyingAndGainsLife() {
        Permanent rabaroo = harness.addToBattlefieldAndReturn(player1, new RabarooTroop());
        harness.setHand(player1, List.of(new Forest()));

        harness.assertLife(player1, 20);
        assertThat(gqs.hasKeyword(gd, rabaroo, Keyword.FLYING)).isFalse();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gqs.hasKeyword(gd, rabaroo, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gqs.hasKeyword(gd, rabaroo, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Rabaroo Troop")
    void opponentLandDoesNotTrigger() {
        Permanent rabaroo = harness.addToBattlefieldAndReturn(player1, new RabarooTroop());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gqs.hasKeyword(gd, rabaroo, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Plainscycling discards Rabaroo Troop and searches for a Plains")
    void plainscyclingSearchesForPlains() {
        harness.setHand(player1, List.of(new RabarooTroop()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rabaroo Troop");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Plains");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Plainscycling requires two mana and does not discard on an unaffordable activation")
    void plainscyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new RabarooTroop()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Rabaroo Troop");
        harness.assertNotInGraveyard(player1, "Rabaroo Troop");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Plainscycling can resolve without finding a Plains and does not draw a card")
    void plainscyclingWithNoPlains() {
        harness.setHand(player1, List.of(new RabarooTroop()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Rabaroo Troop");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Landfall still gains life when Rabaroo Troop leaves before resolution")
    void landfallGainsLifeAfterSourceLeaves() {
        Permanent rabaroo = harness.addToBattlefieldAndReturn(player1, new RabarooTroop());
        harness.setHand(player1, List.of(new Plains()));
        harness.playLand(player1, 0);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(rabaroo);
        gd.playerGraveyards.get(player1.getId()).add(rabaroo.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Rabaroo Troop");
    }
}
