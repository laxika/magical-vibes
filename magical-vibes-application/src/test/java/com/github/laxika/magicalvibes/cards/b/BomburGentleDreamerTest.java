package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BomburGentleDreamer.class, FountainOfYouth.class})
class BomburGentleDreamerTest extends BaseCardTest {

    @Test
    void doesNotUntapBeforeEnduringStory() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        bombur.tap();

        harness.performUntapStep(player1);

        assertThat(bombur.isTapped()).isTrue();
    }

    @Test
    void untapsAfterEnduringStory() {
        Permanent bombur = harness.enterBattlefieldAndReturn(player1, new BomburGentleDreamer());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        bombur.tap();

        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
        harness.performUntapStep(player1);

        assertThat(bombur.isTapped()).isFalse();
    }
}
