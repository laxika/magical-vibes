package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhoulsNightOut.class, GrizzlyBears.class, Island.class})
class GhoulsNightOutTest extends BaseCardTest {

    @Test
    void controllerChoosesOneCreatureFromEachGraveyardAndTheyBecomeBlackDecayedZombies() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Island(), ownCreature));
        harness.setGraveyard(player2, List.of(new Island(), opposingCreature));
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        chooseOnlyCreatureFromGraveyard(player1.getId());
        chooseOnlyCreatureFromGraveyard(player1.getId());

        Permanent ownPermanent = findPermanent(ownCreature);
        Permanent opposingPermanent = findPermanent(opposingCreature);
        assertThat(gqs.hasColor(gd, ownPermanent, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasColor(gd, ownPermanent, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, opposingPermanent, CardColor.BLACK)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(ownPermanent, CardSubtype.ZOMBIE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(opposingPermanent, CardSubtype.ZOMBIE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownPermanent, Keyword.DECAYED)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingPermanent, Keyword.DECAYED)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void ignoresNoncreatureCardsInEachGraveyard() {
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new GhoulsNightOut()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private void chooseOnlyCreatureFromGraveyard(java.util.UUID chooserId) {
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(chooserId);
        assertThat(choice.cardPool()).hasSize(1);
        harness.handleGraveyardCardChosen(player1, 0);
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
