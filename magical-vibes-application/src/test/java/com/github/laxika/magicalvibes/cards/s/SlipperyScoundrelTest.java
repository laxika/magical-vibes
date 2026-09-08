package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlipperyScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have hexproof or unblockability without the city's blessing")
    void noBlessingNoHexproofOrUnblockability() {
        Permanent scoundrel = addCreatureReady(player1, new SlipperyScoundrel());

        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isFalse();
    }

    @Test
    @DisplayName("The city's blessing grants hexproof and unblockability")
    void blessingGrantsHexproofAndUnblockability() {
        Permanent scoundrel = addCreatureReady(player1, new SlipperyScoundrel());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isTrue();
    }

    @Test
    @DisplayName("Entering as the tenth permanent grants the city's blessing")
    void entersAsTenthPermanent() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SlipperyScoundrel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scoundrel = findPermanent(player1, "Slippery Scoundrel");
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isTrue();
    }
}
