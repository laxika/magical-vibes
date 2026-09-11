package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepelTheVile.class, CrawWurm.class, GrizzlyBears.class, GloriousAnthem.class})
class RepelTheVileTest extends BaseCardTest {

    @Test
    void exilesCreatureWithPowerFourOrGreater() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        cast(0, target);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertNotOnBattlefield(player2, "Craw Wurm");
    }

    @Test
    void cannotTargetCreatureWithPowerLessThanFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    void exilesTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1, target);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void enchantmentModeCannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }

    private void cast(int mode, Permanent target) {
        prepareCard();
        harness.castModalInstant(player1, 0, mode, List.of(target.getId()));
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new RepelTheVile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
