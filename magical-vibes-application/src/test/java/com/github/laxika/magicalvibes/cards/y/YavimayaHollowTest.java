package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayaHollow.class, HulkingOgre.class, ElvishLookout.class})
class YavimayaHollowTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability grants a shield to target creature")
    void regeneratesTargetCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability can target an opponent's creature")
    void regeneratesOpponentsCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability can target a creature you control")
    void regeneratesOwnCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HulkingOgre());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new YavimayaHollow());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability requires green mana")
    void regenerationRequiresGreenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(land.isTapped()).isFalse();
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The regeneration ability requires an untapped source")
    void regenerationRequiresUntappedSource() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The regeneration ability cannot target a creature with shroud")
    void cannotTargetCreatureWithShroud() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaHollow());
        Permanent shroudedCreature = harness.addToBattlefieldAndReturn(player2, new ElvishLookout());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, shroudedCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
        assertThat(shroudedCreature.getRegenerationShield()).isZero();
    }
}
