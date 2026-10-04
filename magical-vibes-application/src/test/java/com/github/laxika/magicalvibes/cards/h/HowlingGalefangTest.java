package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BeanstalkWurm;
import com.github.laxika.magicalvibes.cards.p.PlantBeans;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowlingGalefang.class, BeanstalkWurm.class, PlantBeans.class})
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
        gd.addToExile(player1.getId(), new HowlingGalefang());
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

    @Test
    void faceDownAdventureCardDoesNotGrantHaste() {
        Permanent galefang = harness.addToBattlefieldAndReturn(player1, new HowlingGalefang());
        gd.addToExile(player1.getId(), new BeanstalkWurm(), null, true);

        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isFalse();
    }

    @Test
    void onlyGalefangGainsHaste() {
        Permanent galefang = harness.addToBattlefieldAndReturn(player1, new HowlingGalefang());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new BeanstalkWurm());
        Permanent opposingGalefang = harness.addToBattlefieldAndReturn(player2, new HowlingGalefang());
        harness.setExile(player1, List.of(new BeanstalkWurm()));

        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingGalefang, Keyword.HASTE)).isFalse();
    }

    @Test
    void retainsHasteWhileAnotherAdventureCardRemainsInExile() {
        Permanent galefang = harness.addToBattlefieldAndReturn(player1, new HowlingGalefang());
        Card first = new BeanstalkWurm();
        Card second = new BeanstalkWurm();
        harness.setExile(player1, List.of(first, second));

        gd.removeFromExile(first.getId());

        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isTrue();
    }

    @Test
    void gainsHasteAfterAdventureResolvesAndLosesItAsCreatureIsCast() {
        Permanent galefang = harness.addToBattlefieldAndReturn(player1, new HowlingGalefang());
        BeanstalkWurm adventure = new BeanstalkWurm();
        harness.setHand(player1, List.of(adventure));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, adventure.getId());

        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, galefang, Keyword.HASTE)).isFalse();
    }
}
