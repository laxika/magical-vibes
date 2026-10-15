package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PelargirSurvivor.class, Forest.class})
class PelargirSurvivorTest extends BaseCardTest {

    @Test
    void tapAbilityAddsInstantOrSorceryOnlyMana() {
        addReadySurvivor();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        var manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.getInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThat(manaPool.getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void expensiveTapAbilityMillsTargetPlayer() {
        addReadySurvivor();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second, third);
    }

    private Permanent addReadySurvivor() {
        return addCreatureReady(player1, new PelargirSurvivor());
    }
}
