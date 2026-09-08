package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DralnusCrusadeTest extends BaseCardTest {

    @Test
    @DisplayName("Goblins get +1/+1, become black, and gain Zombie in addition to their types")
    void buffsAndChangesGoblins() {
        harness.addToBattlefield(player1, new GoblinEliteInfantry());
        harness.addToBattlefield(player2, new DralnusCrusade());

        Permanent goblin = findPermanent(player1, "Goblin Elite Infantry");

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, goblin))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, goblin))
                .contains(CardSubtype.GOBLIN, CardSubtype.WARRIOR, CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Non-Goblins are not affected")
    void doesNotAffectNonGoblins() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DralnusCrusade());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears))
                .containsExactly(CardSubtype.BEAR);
    }
}
