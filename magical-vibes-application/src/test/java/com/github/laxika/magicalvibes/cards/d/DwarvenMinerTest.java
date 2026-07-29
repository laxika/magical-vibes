package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DwarvenMinerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonbasic land")
    void destroysNonbasicLand() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Can destroy a nonbasic land its own controller owns")
    void canDestroyOwnNonbasicLand() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player1, "Ghost Quarter");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ghost Quarter");
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetCreature() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
