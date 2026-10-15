package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldOfDromoka.class, DromokaWarrior.class, GrizzlyBears.class, Flatten.class})
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

    @Test
    void warriorsAttackWithoutTappingWhileNonWarriorsTap() {
        addCreatureReady(player1, new HeraldOfDromoka());
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());
        Permanent nonWarrior = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));

        assertThat(warrior.isAttacking()).isTrue();
        assertThat(warrior.isTapped()).isFalse();
        assertThat(nonWarrior.isAttacking()).isTrue();
        assertThat(nonWarrior.isTapped()).isTrue();
    }

    @Test
    void warriorsLoseGrantedVigilanceWhenHeraldDies() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfDromoka());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new DromokaWarrior());
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.VIGILANCE)).isTrue();
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, herald.getId());

        harness.assertInGraveyard(player1, "Herald of Dromoka");
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.VIGILANCE)).isFalse();
    }
}
