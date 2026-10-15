package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StampedingHorncrest.class, SunCrestedPterodon.class, SunSentinel.class})
class StampedingHorncrestTest extends BaseCardTest {

    @Test
    void hasHasteWithAnotherDinosaur() {
        Permanent horncrest = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());
        harness.addToBattlefield(player1, new SunCrestedPterodon());

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotHaveHasteWithoutAnotherDinosaur() {
        Permanent horncrest = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();
    }

    @Test
    void nonDinosaurDoesNotGrantHaste() {
        Permanent horncrest = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());
        harness.addToBattlefield(player1, new SunSentinel());

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();
    }

    @Test
    void opponentDinosaurDoesNotGrantHaste() {
        Permanent horncrest = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());
        harness.addToBattlefield(player2, new SunCrestedPterodon());

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();
    }

    @Test
    void losesHasteWhenTheOtherDinosaurLeaves() {
        Permanent horncrest = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());
        harness.addToBattlefield(player1, new SunCrestedPterodon());
        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Sun-Crested Pterodon"));

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();
    }

    @Test
    void gainsHasteWhenAnotherDinosaurEntersLater() {
        Permanent horncrest = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());
        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isFalse();

        harness.addToBattlefield(player1, new SunCrestedPterodon());

        assertThat(gqs.hasKeyword(gd, horncrest, Keyword.HASTE)).isTrue();
    }

    @Test
    void twoHorncrestsGrantEachOtherHaste() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StampedingHorncrest());

        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isFalse();
    }
}
