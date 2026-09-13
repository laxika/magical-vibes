package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReclusiveWight.class, CoralMerfolk.class, Forest.class, WornPowerstone.class})
class ReclusiveWightTest extends BaseCardTest {

    @Test
    @DisplayName("Alone, survives its upkeep trigger")
    void survivesAlone() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new ReclusiveWight());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(wight.getId()));
    }

    @Test
    @DisplayName("A controlled nonland permanent causes it to be sacrificed")
    void sacrificesWithAnotherNonlandPermanent() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new ReclusiveWight());
        harness.addToBattlefield(player1, new CoralMerfolk());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(wight.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wight.getCard());
    }

    @Test
    @DisplayName("A controlled noncreature artifact causes it to be sacrificed")
    void sacrificesWithAnotherNoncreatureArtifact() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new ReclusiveWight());
        harness.addToBattlefield(player1, new WornPowerstone());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(wight.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wight.getCard());
    }

    @Test
    @DisplayName("A land or an opponent's nonland permanent does not count")
    void ignoresLandsAndOpponentPermanents() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new ReclusiveWight());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new CoralMerfolk());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(wight.getId()));
    }

    @Test
    @DisplayName("Rechecks the condition when the trigger resolves")
    void rechecksConditionAtResolution() {
        Permanent wight = harness.addToBattlefieldAndReturn(player1, new ReclusiveWight());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(merfolk);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(wight.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wight.getCard());
    }
}
