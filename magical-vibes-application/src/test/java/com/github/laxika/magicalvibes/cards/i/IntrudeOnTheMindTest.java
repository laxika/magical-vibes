package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntrudeOnTheMind.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class IntrudeOnTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent chooses the hand pile and the Thopter gets counters for the graveyard pile")
    void opponentChoosesPileAndCreatesCounteredThopter() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card mountain = new Mountain();
        IntrudeOnTheMind spell = new IntrudeOnTheMind();
        harness.setLibrary(player1, List.of(island, forest, swamp, plains, mountain));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation).isNotNull();
        assertThat(separation.playerId()).isEqualTo(player1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(island.getId(), forest.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> !card.getId().equals(spell.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(swamp.getId(), plains.getId(), mountain.getId());

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent thopter = tokens.getFirst();
        assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
        assertThat(thopter.getCard().getPower()).isZero();
        assertThat(thopter.getCard().getToughness()).isZero();
        assertThat(thopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(thopter.getCard().getColor()).isNull();
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }
}
