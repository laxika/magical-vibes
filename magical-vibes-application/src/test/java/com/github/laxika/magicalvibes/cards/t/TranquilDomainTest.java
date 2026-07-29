package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Weakness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TranquilDomainTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every non-Aura enchantment on both battlefields")
    void destroysNonAuraEnchantments() {
        harness.addToBattlefield(player1, new BadMoon());
        harness.addToBattlefield(player2, new Crusade());
        castTranquilDomain();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaves Auras and non-enchantment permanents alone")
    void leavesAurasAndOtherPermanentsAlone() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Weakness()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        castTranquilDomain();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Weakness");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Forest");
    }

    @Test
    @DisplayName("Resolves with no enchantments on the battlefield")
    void resolvesWithNoEnchantments() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castTranquilDomain();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
    }

    private void castTranquilDomain() {
        harness.setHand(player1, List.of(new TranquilDomain()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
