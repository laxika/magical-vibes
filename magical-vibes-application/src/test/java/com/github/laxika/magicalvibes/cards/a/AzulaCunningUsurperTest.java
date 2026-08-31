package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({AzulaCunningUsurper.class, GiantSpider.class, GrizzlyBears.class, Island.class})
class AzulaCunningUsurperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a nontoken creature and a nonland graveyard card, tracking both with Azula")
    void etbExilesAndTracksCards() {
        Card creature = new GiantSpider();
        Card graveyardLand = new Island();
        Card graveyardCard = new GrizzlyBears();
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setGraveyard(player2, List.of(graveyardLand, graveyardCard));

        Permanent azula = castAzula(player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(graveyardLand.getId());
        assertThat(gd.getCardsExiledByPermanent(azula.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), graveyardCard.getId());
    }

    @Test
    @DisplayName("Firebending adds red mana through combat and empties it afterward")
    void firebendingAddsManaUntilEndOfCombat() {
        Permanent azula = addReadyAzula();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(azula);
    }

    @Test
    @DisplayName("The controller may cast an exiled card with any mana during their turn as though it had flash")
    void castsTrackedCardWithAnyManaAndFlash() {
        Card graveyardCard = new GrizzlyBears();
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setGraveyard(player2, List.of(graveyardCard));
        Permanent azula = castAzula(player2.getId());
        Card exiledCard = gd.getCardsExiledByPermanent(azula.getId()).stream()
                .filter(card -> card.getId().equals(graveyardCard.getId()))
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCard.getId()));
    }

    @Test
    @DisplayName("Tracked cards cannot be cast during an opponent's turn")
    void cannotCastTrackedCardDuringOpponentsTurn() {
        Card graveyardCard = new GrizzlyBears();
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setGraveyard(player2, List.of(graveyardCard));
        Permanent azula = castAzula(player2.getId());
        Card exiledCard = gd.getCardsExiledByPermanent(azula.getId()).stream()
                .filter(card -> card.getId().equals(graveyardCard.getId()))
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castAzula(java.util.UUID targetPlayerId) {
        Card azulaCard = new AzulaCunningUsurper();
        harness.setHand(player1, List.of(azulaCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(azulaCard.getId()))
                .findFirst()
                .orElseThrow();
    }

    private Permanent addReadyAzula() {
        Permanent azula = harness.addToBattlefieldAndReturn(player1, new AzulaCunningUsurper());
        azula.setSummoningSick(false);
        return azula;
    }
}
