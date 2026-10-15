package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CityOfShadows;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WordOfBinding.class, Squire.class, CityOfShadows.class})
class WordOfBindingTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps both target creatures")
    void tapsAllTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Squire());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Squire());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2: {2}{B}{B}

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires exactly X targets")
    void requiresExactlyXTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Squire());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfShadows());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1: {1}{B}{B}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void zeroXResolvesWithoutTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Squire());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Word of Binding");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Squire());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnCreatureAndAlreadyTappedCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Squire());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Squire());
        opposing.tap();
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new Squire());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2, List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();

        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Word of Binding");
    }

    @Test
    void remainingLegalTargetIsTappedWhenOtherTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Squire());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Squire());
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Word of Binding");
    }

    @Test
    void xAboveOneHundredStillRequiresExactlyXTargets() {
        List<java.util.UUID> targets = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new Squire()).getId());
        }
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 103);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 101, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTapMoreThanOneHundredCreatures() {
        List<Permanent> creatures = new java.util.ArrayList<>();
        for (int i = 0; i < 101; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player2, new Squire()));
        }
        harness.setHand(player1, List.of(new WordOfBinding()));
        harness.addMana(player1, ManaColor.BLACK, 103);

        harness.castSorcery(player1, 0, 101, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(creatures).allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Word of Binding");
    }
}
