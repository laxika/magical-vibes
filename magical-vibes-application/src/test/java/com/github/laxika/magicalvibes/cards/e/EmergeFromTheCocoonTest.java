package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SilentHallcreeper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmergeFromTheCocoon.class, SilentHallcreeper.class})
class EmergeFromTheCocoonTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature from the graveyard and gains 3 life")
    void returnsCreatureAndGainsLife() {
        Card creature = new SilentHallcreeper();
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new EmergeFromTheCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silent Hallcreeper");
        harness.assertNotInGraveyard(player1, "Silent Hallcreeper");
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Emerge from the Cocoon");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .allMatch(p -> !p.isTapped());
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        Card sorcery = new EmergeFromTheCocoon();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new EmergeFromTheCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentsCreature() {
        Card creature = new SilentHallcreeper();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new EmergeFromTheCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without a creature target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new EmergeFromTheCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not gain life when the target leaves the graveyard before resolution")
    void illegalTargetPreventsLifeGain() {
        Card creature = new SilentHallcreeper();
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new EmergeFromTheCocoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Silent Hallcreeper");
        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Emerge from the Cocoon");
    }
}
