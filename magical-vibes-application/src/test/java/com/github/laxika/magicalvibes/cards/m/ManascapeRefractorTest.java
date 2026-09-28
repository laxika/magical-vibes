package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VituGhaziTheCityTree;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManascapeRefractor.class, Forest.class, VituGhaziTheCityTree.class})
class ManascapeRefractorTest extends BaseCardTest {

    @Test
    void entersTapped() {
        Permanent refractor = harness.enterBattlefieldAndReturn(player1, new ManascapeRefractor());

        assertThat(refractor.isTapped()).isTrue();
    }

    @Test
    void copiesManaAbilitiesFromLandsOnEitherBattlefield() {
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new ManascapeRefractor());
        harness.addToBattlefield(player2, new Forest());
        refractor.untap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(refractor.isTapped()).isTrue();
    }

    @Test
    void copiesColoredActivatedAbilitiesAndPaysTheirCostsWithAnyColorMana() {
        Permanent refractor = harness.addToBattlefieldAndReturn(player1, new ManascapeRefractor());
        harness.addToBattlefield(player2, new VituGhaziTheCityTree());
        refractor.untap();
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Saproling"));
    }
}
