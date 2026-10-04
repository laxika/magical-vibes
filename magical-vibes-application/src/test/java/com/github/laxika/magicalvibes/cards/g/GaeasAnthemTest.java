package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CitanulWoodreaders;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasAnthem.class, CitanulWoodreaders.class, GiantDustwasp.class, Opalescence.class})
class GaeasAnthemTest extends BaseCardTest {

    @Test
    void boostsAllCreaturesControllerOwns() {
        harness.addToBattlefield(player1, new GaeasAnthem());
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());
        Permanent dustwasp = harness.addToBattlefieldAndReturn(player1, new GiantDustwasp());

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, dustwasp)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dustwasp)).isEqualTo(4);
    }

    @Test
    void doesNotBoostOpponentsCreatures() {
        harness.addToBattlefield(player1, new GaeasAnthem());
        Permanent opponentWoodreaders = harness.addToBattlefieldAndReturn(player2, new CitanulWoodreaders());

        assertThat(gqs.getEffectivePower(gd, opponentWoodreaders)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentWoodreaders)).isEqualTo(4);
    }

    @Test
    void doesNotBoostCreaturesWhenOpponentControlsTheAnthem() {
        harness.addToBattlefield(player2, new GaeasAnthem());
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(4);
    }

    @Test
    void bonusIsRemovedWhenAnthemLeavesBattlefield() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GaeasAnthem());
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(anthem);

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(4);
    }

    @Test
    void boostsExistingCreaturesOnlyAfterResolving() {
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());

        harness.castFromHand(player1, new GaeasAnthem(), "{1}{G}{G}");

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(5);
    }

    @Test
    void multipleAnthemsStackAndRemovingOneLeavesTheOtherBonus() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GaeasAnthem());
        harness.addToBattlefield(player1, new GaeasAnthem());
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(5);
    }

    @Test
    @CardUsed({GaeasAnthem.class, Opalescence.class})
    void boostsItselfWhenOpalescenceMakesItACreature() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GaeasAnthem());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, anthem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, anthem)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anthem)).isEqualTo(4);
    }
}
