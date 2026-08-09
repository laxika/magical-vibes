package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContemplationTest extends BaseCardTest {

    @Test
    void controllerGainsLifeWhenCastingASpell() {
        harness.addToBattlefield(player1, new Contemplation());
        harness.setHand(player1, List.of(new Spellbook()));

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Contemplation"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void eachCastTriggersLifeGain() {
        harness.addToBattlefield(player1, new Contemplation());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
