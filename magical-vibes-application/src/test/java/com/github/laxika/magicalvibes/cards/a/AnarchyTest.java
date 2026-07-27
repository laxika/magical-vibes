package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnarchyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys white permanents of any type and spares non-white ones")
    void destroysOnlyWhitePermanents() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Plains());
        castAnarchy();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Ornithopter", "Plains");
    }

    @Test
    @DisplayName("Spares Plains — a land has no mana cost, so it is colorless (CR 202.2)")
    void sparesPlains() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Plains());
        castAnarchy();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Plains", "Plains");
    }

    @Test
    @DisplayName("Destroys white non-creature permanents such as Auras")
    void destroysWhiteEnchantments() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Pacifism());
        castAnarchy();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The caster's own white permanents are destroyed too")
    void casterIsNotSpared() {
        harness.addToBattlefield(player1, new SerraAngel());
        castAnarchy();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Serra Angel"));
    }

    private void castAnarchy() {
        harness.setHand(player1, List.of(new Anarchy()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
