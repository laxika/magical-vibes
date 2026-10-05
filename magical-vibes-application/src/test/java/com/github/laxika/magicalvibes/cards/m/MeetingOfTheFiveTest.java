package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CelestialDawn;
import com.github.laxika.magicalvibes.cards.l.LagrellaTheMagpie;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnoopingNewsie;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeetingOfTheFive.class, LagrellaTheMagpie.class, Plains.class,
        SnoopingNewsie.class, CelestialDawn.class})
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

    @Test
    void castsExiledThreeColorCreatureWithRestrictedColoredMana() {
        Card creature = new LagrellaTheMagpie();
        Card twoColorCreature = new SnoopingNewsie();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(creature, twoColorCreature, land));
        castMeeting();

        assertThatThrownBy(() -> harness.castFromExile(player1, twoColorCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(creature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(twoColorCreature, land);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getExactlyThreeColorSpellOnlyManaTotal()).isEqualTo(7);
    }

    @Test
    void exilesOnlyTenCardsAndLeavesEleventhInLibrary() {
        List<Card> cards = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> (Card) new Plains()).toList();
        harness.setLibrary(player1, cards);
        castMeeting();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyElementsOf(cards.subList(0, 10));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cards.get(10));
    }

    @Test
    void emptyLibraryStillProducesRestrictedMana() {
        harness.setLibrary(player1, List.of());
        castMeeting();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getExactlyThreeColorSpellOnlyManaTotal()).isEqualTo(10);
    }

    @Test
    void cannotCastExiledSpellThatHasBecomeMonocolored() {
        Card creature = new LagrellaTheMagpie();
        harness.setLibrary(player1, List.of(creature));
        castMeeting();
        harness.addToBattlefield(player1, new CelestialDawn());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gqs.getEffectiveCardColors(gd, creature)).containsExactly(CardColor.WHITE);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void uncastCardsRemainExiledButPermissionExpiresAfterTurn() {
        Card creature = new LagrellaTheMagpie();
        harness.setLibrary(player1, List.of(creature));
        castMeeting();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId())
                .getExactlyThreeColorSpellOnlyManaTotal()).isZero();
    }

    private void castMeeting() {
        harness.castFromHand(player1, new MeetingOfTheFive(), "{3}{W}{U}{B}{R}{G}");
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
