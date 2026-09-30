package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronrootTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TopsoilTurner.class, Forest.class, IronrootTreefolk.class, GrizzlyBears.class})
class TopsoilTurnerTest extends BaseCardTest {

    @Test
    void perpetuallyGrantsTheManaAbilityToForestsAndTreefolkInHand() {
        Forest forest = new Forest();
        IronrootTreefolk treefolk = new IronrootTreefolk();
        GrizzlyBears nonmatching = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, treefolk, nonmatching));

        harness.enterBattlefieldAndReturn(player1, new TopsoilTurner());
        resolveAllTriggers();

        Permanent forestPermanent = harness.enterBattlefieldAndReturn(player1, forest);
        Permanent treefolkPermanent = harness.enterBattlefieldAndReturn(player1, treefolk);
        Permanent nonmatchingPermanent = harness.enterBattlefieldAndReturn(player1, nonmatching);
        treefolkPermanent.setSummoningSick(false);

        assertThat(gs.getEffectiveActivatedAbilities(gd, forestPermanent)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, treefolkPermanent)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, nonmatchingPermanent)).isEmpty();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(forestPermanent), null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
