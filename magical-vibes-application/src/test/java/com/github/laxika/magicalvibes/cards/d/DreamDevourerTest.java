package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreamDevourerTest extends BaseCardTest {

    @Test
    void grantsForetellWithReducedCostAndBoostsWhenCardIsForetold() {
        Permanent dreamDevourer = addCreatureReady(player1, new DreamDevourer());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);
        resolveAllTriggers();

        ExiledCardEntry entry = gd.findExiledCard(bears.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, dreamDevourer)).isEqualTo(2);

        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromExile(player1, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
