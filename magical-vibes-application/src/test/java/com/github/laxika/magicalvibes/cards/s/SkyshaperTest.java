package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkyshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Skyshaper gives your creatures flying until end of turn")
    void sacrificesSourceAndGrantsFlyingToOwnCreatures() {
        addSkyshaper();
        Permanent ownCreature = addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreature(player2);

        activateSkyshaper();

        harness.assertInGraveyard(player1, "Skyshaper");
        assertThat(ownCreature.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Skyshaper's flying grant wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        addSkyshaper();
        Permanent ownCreature = addReadyCreature(player1);

        activateSkyshaper();
        assertThat(ownCreature.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    private void addSkyshaper() {
        harness.addToBattlefield(player1, new Skyshaper());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new GrizzlyBears());
        Permanent creature = findPermanent(player, "Grizzly Bears");
        creature.setSummoningSick(false);
        return creature;
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
