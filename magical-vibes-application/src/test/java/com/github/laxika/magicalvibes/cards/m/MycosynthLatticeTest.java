package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MycosynthLatticeTest extends BaseCardTest {

    @Test
    @DisplayName("Makes every battlefield permanent an artifact and colorless")
    void makesBattlefieldPermanentsArtifactsAndColorless() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new MycosynthLattice());

        assertThat(gqs.isArtifact(gd, ownCreature)).isTrue();
        assertThat(gqs.isArtifact(gd, opponentCreature)).isTrue();
        assertThat(gqs.isArtifact(gd, forest)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, ownCreature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, opponentCreature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
    }

    @Test
    @DisplayName("Makes cards outside the battlefield colorless")
    void makesCardsOutsideBattlefieldColorless() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectiveCardColors(gd, gd.playerHands.get(player1.getId()).getFirst()))
                .isEmpty();
    }

    @Test
    @DisplayName("Lets colored spells be cast using mana of any color")
    void castsColoredSpellWithAnyManaColor() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hill Giant")).hasSize(1);
    }
}
