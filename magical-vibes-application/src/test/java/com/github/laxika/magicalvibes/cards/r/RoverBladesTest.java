package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoverBladesTest extends BaseCardTest {

    @Test
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent roverBlades = addRoverBladesReady(player1);
        roverBlades.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void equipAttachesToTargetCreature() {
        Permanent roverBlades = addRoverBladesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(roverBlades.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void crewAnimatesVehicleWithDoubleStrikeAndTapsCrewUntilEndOfTurn() {
        Permanent roverBlades = addRoverBladesReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, roverBlades)).isTrue();
        assertThat(gqs.hasKeyword(gd, roverBlades, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, roverBlades)).isFalse();
    }

    private Permanent addRoverBladesReady(Player player) {
        Permanent permanent = new Permanent(new RoverBlades());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

}
