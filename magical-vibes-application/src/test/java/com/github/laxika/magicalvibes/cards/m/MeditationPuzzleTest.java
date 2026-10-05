package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Soulmender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeditationPuzzle.class, Soulmender.class, Ornithopter.class})
class MeditationPuzzleTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 8 life")
    void gainsEightLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MeditationPuzzle()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Meditation Puzzle");
    }

    @Test
    @DisplayName("Can use convoke to help cast the spell")
    void castsWithConvoke() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new Soulmender());
        harness.setHand(player1, List.of(new MeditationPuzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreature.getId()));
        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 28);
    }

    @Test
    void castsEntirelyWithConvokeIncludingSummoningSickCreatures() {
        Permanent firstWhite = harness.addToBattlefieldAndReturn(player1, new Soulmender());
        Permanent secondWhite = harness.addToBattlefieldAndReturn(player1, new Soulmender());
        Permanent firstColorless = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent secondColorless = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent thirdColorless = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        List<Permanent> creatures = List.of(firstWhite, secondWhite, firstColorless, secondColorless, thirdColorless);
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new MeditationPuzzle()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 28);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Meditation Puzzle");
    }

    @Test
    void whiteCreatureCanPayGenericManaWhenWhiteManaIsAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Soulmender());
        harness.setHand(player1, List.of(new MeditationPuzzle()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertLife(player1, 28);
    }

    @Test
    void colorlessCreatureCannotPayWhiteMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new MeditationPuzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Meditation Puzzle");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedCreatureCannotConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Soulmender());
        creature.tap();
        harness.setHand(player1, List.of(new MeditationPuzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Meditation Puzzle");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsCreatureCannotConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Soulmender());
        harness.setHand(player1, List.of(new MeditationPuzzle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(creature.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Meditation Puzzle");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
