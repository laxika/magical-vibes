package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArborealAlliance.class, WirewoodElf.class, GrizzlyBears.class})
class ArborealAllianceTest extends BaseCardTest {

    @Test
    void createsTreefolkUsingXPaidToCast() {
        castAlliance(2);

        Permanent treefolk = findPermanent(player1, "Treefolk");
        assertThat(treefolk.getEffectivePower()).isEqualTo(2);
        assertThat(treefolk.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void populatesWhenAnElfAttacks() {
        castAlliance(2);
        Permanent elf = addCreatureReady(player1, new WirewoodElf());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(elf)));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treefolk")).hasSize(2);
    }

    @Test
    void doesNotPopulateWhenNoElfAttacks() {
        castAlliance(2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));

        assertThat(findPermanents(player1, "Treefolk")).hasSize(1);
    }

    private void castAlliance(int xValue) {
        harness.setHand(player1, List.of(new ArborealAlliance()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castArtifact(player1, 0, xValue);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
