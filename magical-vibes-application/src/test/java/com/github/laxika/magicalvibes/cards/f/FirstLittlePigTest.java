package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BrawlersPlate;
import com.github.laxika.magicalvibes.cards.t.TheConundrumOfBowls;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirstLittlePig.class, BrawlersPlate.class, TheConundrumOfBowls.class})
class FirstLittlePigTest extends BaseCardTest {

    @Test
    void exilesArtifactUsingGreenManaWithoutTappingOrSacrificingSource() {
        Permanent pig = harness.addToBattlefieldAndReturn(player1, new FirstLittlePig());
        BrawlersPlate artifact = new BrawlersPlate();
        Permanent target = harness.addToBattlefieldAndReturn(player2, artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerExiledCards.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pig);
        assertThat(pig.isTapped()).isFalse();
    }

    @Test
    void exilesOwnEnchantmentUsingWhiteMana() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        TheConundrumOfBowls enchantment = new TheConundrumOfBowls();
        Permanent target = harness.addToBattlefieldAndReturn(player1, enchantment);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(enchantment);
    }

    @Test
    void cannotActivateAgainEvenBeforeFirstActivationResolves() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new BrawlersPlate());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new BrawlersPlate());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, first.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first).contains(second);
    }
}
