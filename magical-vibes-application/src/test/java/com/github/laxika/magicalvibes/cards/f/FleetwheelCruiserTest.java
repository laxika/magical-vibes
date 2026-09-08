package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FleetwheelCruiserTest extends BaseCardTest {

    @Test
    void entersAsAnArtifactCreatureUntilEndOfTurn() {
        harness.setHand(player1, List.of(new FleetwheelCruiser()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cruiser = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, cruiser)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cruiser)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cruiser)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cruiser)).isFalse();
    }

    @Test
    void crewAnimatesCruiserAndTapsTheCrewedCreature() {
        Permanent cruiser = addCruiserReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cruiser)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addCruiserReady(Player player) {
        Permanent permanent = new Permanent(new FleetwheelCruiser());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
