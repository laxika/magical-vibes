package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmptyThePits.class})
class EmptyThePitsTest extends BaseCardTest {

    @Test
    @DisplayName("Delve reduces the generic cost and X creates that many tapped Zombies")
    void delvesAndCreatesTappedZombies() {
        List<Card> graveyard = List.of(new EmptyThePits(), new EmptyThePits(), new EmptyThePits());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new EmptyThePits()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false,
                null, null, null, null, List.of(0, 1, 2));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(zombies).hasSize(2);
        assertThat(zombies).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("X=0 creates no Zombies")
    void zeroXCreatesNoZombies() {
        harness.setHand(player1, List.of(new EmptyThePits()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"));
    }

    @Test
    void paysForBothXSymbolsWithoutDelving() {
        harness.setHand(player1, List.of(new EmptyThePits()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, 3, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3)
                .allSatisfy(p -> {
                    assertThat(p.getCard().isToken()).isTrue();
                    assertThat(p.getCard().getName()).isEqualTo("Zombie");
                    assertThat(p.isTapped()).isTrue();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void delvesEntireGenericCostWithoutChangingX() {
        List<Card> graveyard = List.of(new EmptyThePits(), new EmptyThePits());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new EmptyThePits()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 1, null, null, List.of(), List.of(), false,
                null, null, null, null, List.of(0, 1));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(Permanent::isTapped);
    }

    @Test
    void delveCannotReplaceBlackMana() {
        harness.setGraveyard(player1, List.of(new EmptyThePits(), new EmptyThePits()));
        harness.setHand(player1, List.of(new EmptyThePits()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null,
                List.of(), List.of(), false, null, null, null, null, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
