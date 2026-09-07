package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.c.CrystalVein;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenMiner.class, CrystalVein.class, Forest.class, BayFalcon.class})
class DwarvenMinerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonbasic land")
    void destroysNonbasicLand() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new CrystalVein());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Crystal Vein");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Crystal Vein");
        harness.assertInGraveyard(player2, "Crystal Vein");
        assertThat(findPermanent(player1, "Dwarven Miner").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can destroy a nonbasic land its own controller owns")
    void canDestroyOwnNonbasicLand() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player1, new CrystalVein());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player1, "Crystal Vein");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crystal Vein");
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
        harness.addToBattlefield(player2, new BayFalcon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Bay Falcon");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new CrystalVein());
        UUID targetId = harness.getPermanentId(player2, "Crystal Vein");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new CrystalVein());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Crystal Vein");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        addCreatureReady(player1, new DwarvenMiner());
        harness.addToBattlefield(player2, new CrystalVein());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Crystal Vein");
        findPermanent(player1, "Dwarven Miner").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
