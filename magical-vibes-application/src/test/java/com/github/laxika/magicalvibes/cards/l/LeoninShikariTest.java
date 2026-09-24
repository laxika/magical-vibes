package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArmguardFamiliar;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.v.VulshokMorningstar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninShikari.class, VulshokMorningstar.class, CrazedGoblin.class, ArmguardFamiliar.class})
class LeoninShikariTest extends BaseCardTest {

    @Test
    @DisplayName("You can activate equip abilities during an opponent's turn")
    void allowsEquipAtInstantSpeed() {
        addCreatureReady(player1, new LeoninShikari());
        Permanent morningstar = addCreatureReady(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(morningstar.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Leonin Shikari only affects Equipment its controller controls")
    void onlyAffectsControlledEquipment() {
        addCreatureReady(player2, new LeoninShikari());
        addCreatureReady(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Leonin Shikari does not make reconfigure abilities instant-speed")
    void doesNotMakeReconfigureInstantSpeed() {
        addCreatureReady(player1, new LeoninShikari());
        addCreatureReady(player1, new ArmguardFamiliar());
        Permanent creature = addCreatureReady(player1, new CrazedGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
