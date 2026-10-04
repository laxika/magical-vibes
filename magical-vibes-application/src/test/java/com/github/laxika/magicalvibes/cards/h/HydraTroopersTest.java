package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HydraTroopers.class, Swamp.class})
class HydraTroopersTest extends BaseCardTest {

    @Test
    void createsTappedVillainTokenWithMenaceWithTwoCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new HydraTroopers(), new HydraTroopers()));
        harness.castFromHand(player1, new HydraTroopers(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VILLAIN);
        assertThat(token.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    void millsTwoCardsWithFewerThanTwoCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new HydraTroopers()));
        harness.setLibrary(player1, List.of(new HydraTroopers(), new HydraTroopers(), new HydraTroopers()));
        harness.castFromHand(player1, new HydraTroopers(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    void checksCreatureCountWhenTriggerResolvesRatherThanWhenItTriggers() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp()));
        harness.castFromHand(player1, new HydraTroopers(), "{2}{B}");
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(new HydraTroopers(), new HydraTroopers()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void millsInsteadWhenCreatureCardsLeaveGraveyardBeforeResolution() {
        harness.setGraveyard(player1, List.of(new HydraTroopers(), new HydraTroopers()));
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp()));
        harness.castFromHand(player1, new HydraTroopers(), "{2}{B}");
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void countsOnlyCreatureCardsInControllersGraveyard() {
        harness.setGraveyard(player1, List.of(new HydraTroopers(), new Swamp(), new Swamp()));
        harness.setGraveyard(player2, List.of(new HydraTroopers(), new HydraTroopers()));
        harness.setLibrary(player1, List.of(new Swamp(), new Swamp()));
        harness.castFromHand(player1, new HydraTroopers(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void millsOnlyAvailableCardFromShortLibrary() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new HydraTroopers()));
        harness.castFromHand(player1, new HydraTroopers(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
