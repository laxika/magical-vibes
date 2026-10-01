package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormOfSouls.class, GrizzlyBears.class, Shock.class})
class StormOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns every creature card as a 1/1 Spirit with flying")
    void returnsAllCreatureCardsAsSpirits() {
        Card bears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(bears, secondBears, noncreature));
        harness.setHand(player1, List.of(new StormOfSouls()));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertReturnedCreature(bears);
        assertReturnedCreature(secondBears);
    }

    @Test
    @DisplayName("Exiles Storm of Souls after it resolves")
    void exilesItself() {
        StormOfSouls storm = new StormOfSouls();
        harness.setHand(player1, List.of(storm));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(storm.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void assertReturnedCreature(Card card) {
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, permanent)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
    }
}
