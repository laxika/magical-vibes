package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvangelOfHeliodTest extends BaseCardTest {

    private List<Permanent> soldiers() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Soldier"))
                .toList();
    }

    @Test
    @DisplayName("ETB creates Soldiers equal to your devotion to white")
    void etbCreatesSoldiersEqualToWhiteDevotion() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new EvangelOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(soldiers()).hasSize(4);
    }

    @Test
    @DisplayName("Only white mana symbols among your permanents contribute")
    void etbIgnoresNonWhiteManaSymbols() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new EvangelOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(soldiers()).hasSize(2);
    }
}
