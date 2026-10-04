package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TempleThief;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FruitOfTizerus.class, TempleThief.class})
class FruitOfTizerusTest extends BaseCardTest {

    @Test
    void targetPlayerLosesTwoLife() {
        harness.setHand(player1, List.of(new FruitOfTizerus()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Fruit of Tizerus");
    }

    @Test
    void escapeExilesThreeOtherCardsAndReturnsFruitToGraveyard() {
        FruitOfTizerus fruit = new FruitOfTizerus();
        List<TempleThief> otherCards = List.of(new TempleThief(), new TempleThief(), new TempleThief());
        harness.setGraveyard(player1, List.of(fruit, otherCards.get(0), otherCards.get(1), otherCards.get(2)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(1, 2, 3), null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(fruit);
        harness.assertInGraveyard(player1, "Fruit of Tizerus");
        harness.assertLife(player2, 18);
    }

    @Test
    void escapeRequiresThreeOtherCardsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(new FruitOfTizerus(), new TempleThief(), new TempleThief()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(1, 2), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetACreature() {
        harness.setHand(player1, List.of(new FruitOfTizerus()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        var creature = harness.addToBattlefieldAndReturn(player2, new TempleThief());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItsController() {
        harness.setHand(player1, List.of(new FruitOfTizerus()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void escapeCannotExileTheSpellItself() {
        harness.setGraveyard(player1, List.of(new FruitOfTizerus(),
                new TempleThief(), new TempleThief(), new TempleThief()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null,
                player2.getId(), List.of(), List.of(0, 1, 2), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapeCannotPayOnlyTheNormalManaCost() {
        harness.setGraveyard(player1, List.of(new FruitOfTizerus(),
                new TempleThief(), new TempleThief(), new TempleThief()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null,
                player2.getId(), List.of(), List.of(1, 2, 3), null))
                .isInstanceOf(IllegalStateException.class);
    }
}

