package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoastalTower.class, Forest.class, KavuAggressor.class, TrenchWurm.class})
class TrenchWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonbasic land")
    void destroysNonbasicLand() {
        Permanent wurm = addCreatureReady(player1, new TrenchWurm());
        harness.addToBattlefield(player2, new CoastalTower());
        addActivationMana();
        UUID targetId = harness.getPermanentId(player2, "Coastal Tower");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(wurm.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Coastal Tower");
        harness.assertInGraveyard(player2, "Coastal Tower");
    }

    @Test
    @DisplayName("Can destroy a nonbasic land its own controller owns")
    void canDestroyOwnNonbasicLand() {
        addCreatureReady(player1, new TrenchWurm());
        harness.addToBattlefield(player1, new CoastalTower());
        addActivationMana();
        UUID targetId = harness.getPermanentId(player1, "Coastal Tower");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Coastal Tower");
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        addCreatureReady(player1, new TrenchWurm());
        harness.addToBattlefield(player2, new Forest());
        addActivationMana();
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetCreature() {
        addCreatureReady(player1, new TrenchWurm());
        harness.addToBattlefield(player2, new KavuAggressor());
        addActivationMana();
        UUID targetId = harness.getPermanentId(player2, "Kavu Aggressor");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new TrenchWurm());
        harness.addToBattlefield(player2, new CoastalTower());
        UUID targetId = harness.getPermanentId(player2, "Coastal Tower");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the required red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new TrenchWurm());
        harness.addToBattlefield(player2, new CoastalTower());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        UUID targetId = harness.getPermanentId(player2, "Coastal Tower");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new TrenchWurm());
        harness.addToBattlefield(player2, new CoastalTower());
        addActivationMana();
        UUID targetId = harness.getPermanentId(player2, "Coastal Tower");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhenTapped() {
        addCreatureReady(player1, new TrenchWurm());
        harness.addToBattlefield(player2, new CoastalTower());
        addActivationMana();
        UUID targetId = harness.getPermanentId(player2, "Coastal Tower");
        findPermanent(player1, "Trench Wurm").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
