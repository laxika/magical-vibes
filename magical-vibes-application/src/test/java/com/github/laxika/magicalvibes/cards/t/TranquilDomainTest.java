package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArmorOfThorns;
import com.github.laxika.magicalvibes.cards.c.CadaverousBloom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TranquilDomain.class, ArmorOfThorns.class, CadaverousBloom.class, Forest.class, GiantMantis.class})
class TranquilDomainTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys every non-Aura enchantment on both battlefields")
    void destroysNonAuraEnchantments() {
        harness.addToBattlefield(player1, new CadaverousBloom());
        harness.addToBattlefield(player2, new CadaverousBloom());
        castTranquilDomain();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaves Auras and non-enchantment permanents alone")
    void leavesAurasAndOtherPermanentsAlone() {
        Permanent mantis = harness.addToBattlefieldAndReturn(player2, new GiantMantis());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ArmorOfThorns()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, mantis.getId());
        harness.passBothPriorities();
        castTranquilDomain();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Armor of Thorns");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Giant Mantis", "Forest");
    }

    @Test
    @DisplayName("Resolves with no enchantments on the battlefield")
    void resolvesWithNoEnchantments() {
        harness.addToBattlefield(player2, new GiantMantis());
        castTranquilDomain();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Giant Mantis");
    }

    private void castTranquilDomain() {
        harness.castFromHand(player1, new TranquilDomain(), "{1}{G}");
        harness.passBothPriorities();
    }
}
