package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MeetingOfTheFive.class)
class MeetingOfTheFiveTest extends BaseCardTest {

    @Test
    void exilesTopTenAndPermitsExactlyThreeColorSpells() {
        Card threeColorSpell = spell("Three-color spell", List.of(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK));
        Card twoColorSpell = spell("Two-color spell", List.of(CardColor.WHITE, CardColor.BLUE));
        Card land = new Card();
        land.setName("Land");
        land.setType(CardType.LAND);
        List<Card> topCards = List.of(
                threeColorSpell, twoColorSpell, land,
                spell("Spell four", List.of(CardColor.RED)),
                spell("Spell five", List.of(CardColor.GREEN)),
                spell("Spell six", List.of()),
                spell("Spell seven", List.of(CardColor.WHITE, CardColor.BLUE, CardColor.RED)),
                spell("Spell eight", List.of(CardColor.BLACK)),
                spell("Spell nine", List.of(CardColor.WHITE, CardColor.BLACK)),
                spell("Spell ten", List.of(CardColor.GREEN)));
        harness.setLibrary(player1, topCards);
        castMeeting();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(topCards);
        assertThat(gd.exilePlayPermissions).containsEntry(threeColorSpell.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions)
                .doesNotContainKeys(twoColorSpell.getId(), land.getId());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getExactlyThreeColorSpellOnlyMana(ManaColor.WHITE)).isEqualTo(2);
        assertThat(pool.getExactlyThreeColorSpellOnlyMana(ManaColor.BLUE)).isEqualTo(2);
        assertThat(pool.getExactlyThreeColorSpellOnlyMana(ManaColor.BLACK)).isEqualTo(2);
        assertThat(pool.getExactlyThreeColorSpellOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThat(pool.getExactlyThreeColorSpellOnlyMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void restrictedManaCanCastExactlyThreeColorSpellsOnly() {
        castMeeting();

        Card threeColorSpell = spell("Three-color spell", List.of(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK));
        harness.setHand(player1, List.of(threeColorSpell));
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getExactlyThreeColorSpellOnlyManaTotal()).isEqualTo(7);

        Card twoColorSpell = spell("Two-color spell", List.of(CardColor.WHITE, CardColor.BLUE));
        harness.setHand(player1, List.of(twoColorSpell));
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMeeting() {
        harness.setHand(player1, List.of(new MeetingOfTheFive()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Card spell(String name, List<CardColor> colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{3}");
        card.setColors(colors);
        return card;
    }
}
