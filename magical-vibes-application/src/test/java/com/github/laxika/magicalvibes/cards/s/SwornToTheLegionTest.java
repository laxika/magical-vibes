package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwornToTheLegion.class, GrizzlyBears.class})
class SwornToTheLegionTest extends BaseCardTest {

    @Test
    void givesDoubleTeamToNontokenCreaturesAlreadyOnTheBattlefield() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);

        harness.enterBattlefieldAndReturn(player1, new SwornToTheLegion());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_TEAM)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void givesDoubleTeamToCreatureSpellsYouCast() {
        harness.addToBattlefield(player1, new SwornToTheLegion());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent permanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.DOUBLE_TEAM)).isTrue();
    }
}
