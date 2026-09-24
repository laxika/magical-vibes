package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.FaerieHarbinger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilvergillDouser.class, CoralMerfolk.class, FaerieHarbinger.class, GrizzlyBears.class})
class SilvergillDouserTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces target power by the number of Merfolk and/or Faeries controlled")
    void reducesByMerfolkAndFaerieCount() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player1, new FaerieHarbinger());
        // Player1 controls 3 counted permanents: Douser (Merfolk), Coral Merfolk, Faerie Harbinger

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts only Merfolk and Faeries, not other creatures")
    void countsOnlyMerfolkAndFaeries() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // Only the Douser itself counts (1); the Grizzly Bears does not

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOff() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

}
