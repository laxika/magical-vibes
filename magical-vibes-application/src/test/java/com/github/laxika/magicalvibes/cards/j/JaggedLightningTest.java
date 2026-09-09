package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PlatedWurm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaggedLightning.class, BearCub.class, Mountain.class, PlatedWurm.class})
class JaggedLightningTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3); // {3}{R}{R}
    }

    @Test
    @DisplayName("Deals 3 damage to each of two target creatures, destroying both 2/2s")
    void destroysBothTargets() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(a.getId(), b.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Bear Cub")).hasSize(2);
    }

    @Test
    @DisplayName("Each target takes 3 damage independently; a 4/5 survives with damage marked")
    void marksDamageOnSurvivor() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new PlatedWurm());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(survivor.getId(), bear.getId()));

        assertThat(survivor.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Plated Wurm");
        harness.assertNotOnBattlefield(player2, "Bear Cub");
    }

    @Test
    @DisplayName("Can target creatures controlled by either player")
    void targetsCreaturesRegardlessOfController() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(ownBear.getId(), opposingBear.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Bear Cub")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Bear Cub")).hasSize(1);
    }

    @Test
    @DisplayName("Requires two targets; casting with a single target is rejected")
    void requiresTwoTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        List<UUID> single = List.of(bear.getId());
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, single))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void requiresDistinctTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BearCub());
        harness.addToBattlefield(player1, new Mountain());
        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.setHand(player1, List.of(new JaggedLightning()));
        giveMana();

        List<UUID> targets = List.of(creature.getId(), mountainId);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }
}
