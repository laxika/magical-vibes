package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NaturesChosenTest extends BaseCardTest {

    private Permanent enchanted;

    private void enchant(Permanent creature) {
        enchanted = creature;
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NaturesChosen());
        aura.setAttachedTo(creature.getId());
    }

    @Test
    @DisplayName("{0} ability untaps the enchanted creature")
    void untapsEnchantedCreature() {
        enchant(addCreatureReady(player1, new GrizzlyBears()));
        enchanted.tap();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The {0} ability can be activated only once each turn")
    void untapAbilityIsOncePerTurn() {
        enchant(addCreatureReady(player1, new GrizzlyBears()));
        enchanted.tap();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        enchanted.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapping a white enchanted creature untaps a target land")
    void tapWhiteCreatureToUntapLand() {
        enchant(addCreatureReady(player1, new EliteVanguard()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        harness.activateAbility(player1, 1, 1, null, land.getId());
        harness.passBothPriorities();

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The untap-target ability cannot be activated when the enchanted creature is not white")
    void requiresWhiteEnchantedCreature() {
        enchant(addCreatureReady(player1, new GrizzlyBears()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap-target ability cannot be activated while the enchanted creature is tapped")
    void requiresUntappedEnchantedCreature() {
        enchant(addCreatureReady(player1, new EliteVanguard()));
        enchanted.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap-target ability can be activated only once each turn")
    void untapTargetAbilityIsOncePerTurn() {
        enchant(addCreatureReady(player1, new EliteVanguard()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();

        harness.activateAbility(player1, 1, 1, null, land.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
