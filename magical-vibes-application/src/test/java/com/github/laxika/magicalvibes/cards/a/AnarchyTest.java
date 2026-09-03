package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Anarchy.class, GrizzlyBears.class, Ornithopter.class, Pacifism.class, Plains.class,
        SerraAngel.class})
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
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(creature.getId());
        castAnarchy();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @CardUsed(AvacynAngelOfHope.class)
    @DisplayName("Does not destroy an indestructible white permanent")
    void sparesIndestructibleWhitePermanent() {
        harness.addToBattlefield(player2, new AvacynAngelOfHope());
        harness.addToBattlefield(player1, new SerraAngel());
        castAnarchy();

        harness.assertOnBattlefield(player2, "Avacyn, Angel of Hope");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
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
        harness.castFromHand(player1, new Anarchy(), "{2}{R}{R}");
        harness.passBothPriorities();
    }
}
