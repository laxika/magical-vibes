package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YavimayaHollowTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new YavimayaHollow());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability grants a shield to target creature")
    void regeneratesTargetCreature() {
        harness.addToBattlefield(player1, new YavimayaHollow());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, creatureId);
        harness.passBothPriorities();

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability can target an opponent's creature")
    void regeneratesOpponentsCreature() {
        harness.addToBattlefield(player1, new YavimayaHollow());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, creatureId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new YavimayaHollow());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
