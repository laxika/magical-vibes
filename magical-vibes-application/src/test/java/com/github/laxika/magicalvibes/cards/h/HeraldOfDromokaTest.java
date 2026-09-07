package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfDromoka.class, DromokaWarrior.class, GrizzlyBears.class})
class HeraldOfDromokaTest extends BaseCardTest {

    @Test
    void grantsVigilanceToOtherWarriorsYouControl() {
        harness.addToBattlefield(player1, new HeraldOfDromoka());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        Permanent opposingWarrior = harness.addToBattlefieldAndReturn(player2, new DromokaWarrior());
        Permanent nonWarrior = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingWarrior, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonWarrior, Keyword.VIGILANCE)).isFalse();
    }
}
