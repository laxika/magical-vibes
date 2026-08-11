package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmptyThePitsTest extends BaseCardTest {

    @Test
    @DisplayName("Delve reduces the generic cost and X creates that many tapped Zombies")
    void delvesAndCreatesTappedZombies() {
        List<Card> graveyard = List.of(new Shock(), new GrizzlyBears(), new Shock());
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
}
