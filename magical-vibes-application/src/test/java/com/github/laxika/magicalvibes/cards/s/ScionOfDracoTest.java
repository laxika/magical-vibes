package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfDraco.class, Forest.class, Island.class, Swamp.class, Mountain.class, Plains.class,
        EliteVanguard.class, FugitiveWizard.class, WalkingCorpse.class, HillGiant.class, GrizzlyBears.class})
class ScionOfDracoTest extends BaseCardTest {

    @Test
    @DisplayName("Domain reduces Scion of Draco's generic cost by two per basic land type")
    void domainReducesCostPerBasicLandType() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new ScionOfDraco()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Grants each controlled creature the keyword matching its color")
    void grantsKeywordsByColorToControlledCreatures() {
        addCreatureReady(player1, new ScionOfDraco());
        Permanent white = addCreatureReady(player1, new EliteVanguard());
        Permanent blue = addCreatureReady(player1, new FugitiveWizard());
        Permanent black = addCreatureReady(player1, new WalkingCorpse());
        Permanent red = addCreatureReady(player1, new HillGiant());
        Permanent green = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGreen = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, white, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, blue, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, black, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, red, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, green, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentGreen, Keyword.TRAMPLE)).isFalse();
    }
}
