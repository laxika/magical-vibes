package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinisterReflections.class, GrizzlyBears.class})
class SinisterReflectionsTest extends BaseCardTest {

    @Test
    void conjuresASeparateDuplicateForEachTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card reflection = new SinisterReflections();
        harness.setHand(player1, List.of(reflection));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        List<Card> duplicates = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .toList();
        assertThat(duplicates).hasSize(2);
        assertThat(duplicates).extracting(Card::getId)
                .doesNotContain(first.getCard().getId(), second.getCard().getId());
        assertThat(duplicates).allMatch(card -> player1.getId().equals(card.getOwnerId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(first, second);
    }

    @Test
    void cannotTargetATokenOrAnOpponentsCreature() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinisterReflections()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(token.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken creature you control");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken creature you control");
    }

    @Test
    void canResolveWithoutChoosingAnyTargets() {
        harness.setHand(player1, List.of(new SinisterReflections()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sinister Reflections");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canConjureOnlyOneDuplicateAndCastItAsACreature() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinisterReflections()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(original.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).contains(original);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void skipsATargetThatChangesControllerWhileDuplicatingTheOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinisterReflections()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).add(first);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotConjureWhenItsOnlyTargetLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinisterReflections()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of(target.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sinister Reflections");
    }

    @Test
    void cannotChooseTheSameCreatureTwiceOrMoreThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinisterReflections()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
