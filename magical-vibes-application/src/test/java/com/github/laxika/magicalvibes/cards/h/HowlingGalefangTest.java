package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BeanstalkWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlantBeans;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowlingGalefang.class, BeanstalkWurm.class, PlantBeans.class, GrizzlyBears.class})
class HowlingGalefangTest extends BaseCardTest {

    @Test
    void doesNotHaveHasteWithoutAnAdventureCardInExile() {
        Permanent galefang = harness.addToBattlefieldAndReturn(player1, new HowlingGalefang());

        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isFalse();
    }

    @Test
    void hasHasteWhenItsControllerOwnsAnAdventureCardInExile() {
        Permanent galefang = harness.addToBattlefieldAndReturn(player1, new HowlingGalefang());
        gd.addToExile(player1.getId(), new BeanstalkWurm());

        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotCountNonAdventureOrOpponentOwnedCards() {
        Permanent galefang = harness.addToBattlefieldAndReturn(player1, new HowlingGalefang());
        gd.addToExile(player1.getId(), new GrizzlyBears());
        gd.addToExile(player2.getId(), new BeanstalkWurm());

        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isFalse();
    }

    @Test
    void losesHasteWhenTheAdventureCardLeavesExile() {
        Permanent galefang = harness.addToBattlefieldAndReturn(player1, new HowlingGalefang());
        Card adventure = new BeanstalkWurm();
        gd.addToExile(player1.getId(), adventure);
        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isTrue();

        gd.removeFromExile(adventure.getId());

        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isFalse();
    }
}
