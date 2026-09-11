package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skyshaper.class, RagingGoblin.class})
class SkyshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Skyshaper gives your creatures flying until end of turn")
    void sacrificesSourceAndGrantsFlyingToOwnCreatures() {
        addSkyshaper();
        Permanent ownCreature = addCreatureReady(player1, new RagingGoblin());
        Permanent opponentCreature = addCreatureReady(player2, new RagingGoblin());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player1, new Skyshaper());

        activateSkyshaper();

        harness.assertInGraveyard(player1, "Skyshaper");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherArtifact, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Skyshaper's flying grant wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addSkyshaper();
        Permanent ownCreature = addCreatureReady(player1, new RagingGoblin());

        activateSkyshaper();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Skyshaper grants flying to creatures present at resolution only")
    void snapshotsCreaturesAtResolution() {
        addSkyshaper();
        Permanent presentAtActivation = addCreatureReady(player1, new RagingGoblin());

        harness.activateAbility(player1, indexOf("Skyshaper"), null, null);
        Permanent presentAtResolution = addCreatureReady(player1, new RagingGoblin());
        harness.passBothPriorities();

        Permanent enteringAfterResolution = addCreatureReady(player1, new RagingGoblin());

        assertThat(gqs.hasKeyword(gd, presentAtActivation, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, presentAtResolution, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, enteringAfterResolution, Keyword.FLYING)).isFalse();
    }

    private void addSkyshaper() {
        harness.addToBattlefield(player1, new Skyshaper());
    }

    private void activateSkyshaper() {
        int index = indexOf("Skyshaper");
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }

    private int indexOf(String name) {
        var battlefield = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Not found: " + name);
    }
}
