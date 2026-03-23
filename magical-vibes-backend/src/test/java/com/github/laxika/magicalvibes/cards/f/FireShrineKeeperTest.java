package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FireShrineKeeperTest extends BaseCardTest {

    private void addReadyKeeper() {
        harness.addToBattlefield(player1, new FireShrineKeeper());
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
    }

    @Test
    @DisplayName("Deals 3 damage to a single target creature")
    void singleTarget() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bearId));
        harness.passBothPriorities();

        // 3 damage kills 2-toughness creature
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 3 damage to each of two target creatures")
    void twoTargets() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(id1, id2));
        harness.passBothPriorities();

        // Both 2-toughness creatures die to 3 damage each
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fire Shrine Keeper is sacrificed as cost when ability is activated")
    void sacrificedAsCost() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bearId));

        // Sacrificed immediately as cost (before resolution)
        harness.assertNotOnBattlefield(player1, "Fire Shrine Keeper");
        harness.assertInGraveyard(player1, "Fire Shrine Keeper");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bearId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires tap — cannot activate if already tapped")
    void cannotActivateIfTapped() {
        addReadyKeeper();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        // Pre-tap the keeper
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bearId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
